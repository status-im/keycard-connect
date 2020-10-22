package im.status.keycard.connect.ui

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.*
import im.status.keycard.connect.data.*
import im.status.keycard.connect.net.WalletConnectListener
import org.walletconnect.Session.Config.Companion.fromWCUri
import kotlin.reflect.KClass

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ScriptListener, WalletConnectListener {
    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var networkSpinner: Spinner
    private lateinit var walletPath: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewSwitcher = ViewSwitcher(this)

        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.activity_main, viewSwitcher)
        inflater.inflate(R.layout.activity_nfc, viewSwitcher)

        setContentView(viewSwitcher)
        Registry.init(this, this, this)
        Registry.scriptExecutor.defaultScript = cardCheckupScript()

        networkSpinner = findViewById(R.id.networkSpinner)
        walletPath = findViewById(R.id.walletPathText)

        ArrayAdapter.createFromResource(this, R.array.networks, R.layout.spinner_item_layout).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            networkSpinner.adapter = it
        }
        networkSpinner.setSelection(CHAIN_IDS.indexOf(Registry.settingsManager.chainID))
        walletPath.setText(Registry.settingsManager.bip32Path)

        handleIntent(intent)
    }

    private fun activateNFC() {
        Registry.nfcAdapter.enableReaderMode(
            this,
            Registry.cardManager,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onResume() {
        super.onResume()
        activateNFC()
    }

    override fun onPause() {
        super.onPause()
        Registry.nfcAdapter.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            handleWCURI(intent.data?.toString())
        }
    }

    override fun onBackPressed() {
        if (viewSwitcher.displayedChild == 0) {
            moveTaskToBack(false)
        } else {
            Registry.scriptExecutor.cancelScript()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_INTERACTIVE_SCRIPT -> Registry.scriptExecutor.onUserInteractionReturned(
                resultCode,
                data
            )
            REQ_WALLETCONNECT -> Registry.walletConnect.onUserInteractionReturned(resultCode, data)
            REQ_LOADKEY -> loadKeyHandler(resultCode, data)
            IntentIntegrator.REQUEST_CODE -> qrCodeScanned(resultCode, data)
        }
    }

    override fun onScriptStarted() {
        this.runOnUiThread {
            activateNFC()
            viewSwitcher.showNext()
        }
    }

    override fun onScriptFinished(result: CardCommand.Result) {
        this.runOnUiThread {
            viewSwitcher.showNext()
            Registry.scriptExecutor.defaultScript = cardCheckupScript()
        }
    }

    fun updateConnection(@Suppress("UNUSED_PARAMETER") view: View) {
        val chainID = CHAIN_IDS[networkSpinner.selectedItemPosition]
        Registry.settingsManager.chainID = chainID
        Registry.ethereumRPC.changeEndpoint(Registry.settingsManager.rpcEndpoint)

        val bip32Path = walletPath.text.toString()
        Registry.settingsManager.bip32Path = bip32Path

        Registry.walletConnect.updateChainAndDerivation(bip32Path, chainID)
    }

    fun cancelNFC(@Suppress("UNUSED_PARAMETER") view: View) {
        Registry.scriptExecutor.cancelScript()
    }

    fun connectWallet(view: View) {
        updateConnection(view)
        val integrator = IntentIntegrator(this)
        integrator.captureActivity = QRCodeActivity::class.java
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    fun disconnectWallet(@Suppress("UNUSED_PARAMETER") view: View) {
        Registry.walletConnect.disconnect()
    }

    fun changePIN(@Suppress("UNUSED_PARAMETER") view: View) {
        startCommand(ChangePINActivity::class)
    }

    fun changePUK(@Suppress("UNUSED_PARAMETER") view: View) {
        startCommand(ChangePUKActivity::class)
    }

    fun changePairingPassword(@Suppress("UNUSED_PARAMETER") view: View) {
        startCommand(ChangePairingPasswordActivity::class)
    }

    fun unpair(@Suppress("UNUSED_PARAMETER") view: View) {
        Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(UnpairCommand()))
    }

    fun unpairOthers(@Suppress("UNUSED_PARAMETER") view: View) {
        Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(UnpairOthersCommand()))
    }

    fun changeKey(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(this, LoadKeyActivity::class.java)
        this.startActivityForResult(intent, REQ_LOADKEY)
    }

    fun removeKey(@Suppress("UNUSED_PARAMETER") view: View) {
        Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(RemoveKeyCommand()))
    }

    fun reinstall(@Suppress("UNUSED_PARAMETER") view: View) {
        startCommand(ReinstallActivity::class)
    }

    private fun loadKeyHandler(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        val loadType = data.getIntExtra(LOAD_TYPE, LOAD_NONE)
        val mnemonic = data.getStringExtra(LOAD_MNEMONIC)

        Registry.scriptExecutor.runScript(
            scriptWithAuthentication().plus(
                LoadKeyCommand(
                    loadType,
                    mnemonic
                )
            )
        )
    }

    private fun startCommand(activity: KClass<out Activity>) {
        val intent = Intent(this, activity.java)
        startActivity(intent)
    }

    private fun qrCodeScanned(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            Log.e(TAG, "QRCode result: $resultCode")
        } else {
            handleWCURI(data.getStringExtra(Intents.Scan.RESULT))
        }
    }

    private fun handleWCURI(uri: String?) {
        if (uri != null) {
            Log.d(TAG, "Connecting to $uri")
            try {
                Registry.walletConnect.connect(fromWCUri(uri).toFullyQualifiedConfig())
            } catch (e: Exception) {
                Log.e(TAG, "Parsing $uri failed", e)
            }
        } else {
            Log.e(TAG, "Null URI received")
        }
    }

    override fun onConnected() {
        this.runOnUiThread {
            val button = findViewById<Button>(R.id.walletConnectButton)
            button.setOnClickListener(this::disconnectWallet)
            button.text = getString(R.string.disconnect_wallet)
        }
    }

    override fun onDisconnected() {
        this.runOnUiThread {
            val button = findViewById<Button>(R.id.walletConnectButton)
            button.setOnClickListener(this::connectWallet)
            button.text = getString(R.string.connect_wallet)
        }
    }

    override fun onAccountChanged(account: String?) {
        this.runOnUiThread {
            if (account == null) {
                findViewById<TextView>(R.id.walletAddress).text = getString(R.string.wallet_not_connected)
            } else {
                findViewById<TextView>(R.id.walletAddress).text = account
            }
        }
    }
}
