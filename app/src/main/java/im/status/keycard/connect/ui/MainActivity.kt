package im.status.keycard.connect.ui

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.*
import im.status.keycard.connect.data.*
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity(), ScriptListener {
    private lateinit var viewSwitcher: ViewSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewSwitcher = ViewSwitcher(this)

        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.activity_main, viewSwitcher)
        inflater.inflate(R.layout.activity_nfc, viewSwitcher)

        setContentView(viewSwitcher)
        Registry.init(this, this)
        Registry.scriptExecutor.defaultScript = cardCheckupScript()
    }

    override fun onResume() {
        super.onResume()
        Registry.nfcAdapter.enableReaderMode(this, Registry.cardManager,NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
    }

    override fun onPause() {
        super.onPause()
        Registry.nfcAdapter.disableReaderMode(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_INTERACTIVE_SCRIPT -> Registry.scriptExecutor.onUserInteractionReturned(resultCode, data)
            REQ_WALLETCONNECT -> Registry.walletConnect.onUserInteractionReturned(resultCode, data)
            REQ_LOADKEY -> loadKeyHandler(resultCode, data)
            IntentIntegrator.REQUEST_CODE -> qrCodeScanned(resultCode, data)
        }
    }

    override fun onScriptStarted() {
        this.runOnUiThread {
            viewSwitcher.showNext()
        }
    }

    override fun onScriptFinished(result: CardCommand.Result) {
        this.runOnUiThread {
            viewSwitcher.showNext()
            Registry.scriptExecutor.defaultScript = cardCheckupScript()
        }
    }

    fun cancelNFC(@Suppress("UNUSED_PARAMETER") view: View) {
        Registry.scriptExecutor.cancelScript()
    }

    fun connectWallet(@Suppress("UNUSED_PARAMETER") view: View) {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
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

    fun settings(@Suppress("UNUSED_PARAMETER") view: View) {
        startCommand(SettingsActivity::class)
    }

    private fun loadKeyHandler(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        val loadType = data.getIntExtra(LOAD_TYPE, LOAD_NONE)
        val mnemonic = data.getStringExtra(LOAD_MNEMONIC)

        Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(LoadKeyCommand(loadType, mnemonic)))
    }

    private fun startCommand(activity: KClass<out Activity>) {
        val intent = Intent(this, activity.java)
        startActivity(intent)
    }

    private fun qrCodeScanned(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        val uri: String? = data.getStringExtra(Intents.Scan.RESULT)

        if (uri != null && uri.startsWith("wc:")) {
            Registry.walletConnect.connect(uri)
        }
    }
}
