package im.status.keycard.connect

import android.annotation.SuppressLint
import android.app.Activity
import android.nfc.NfcAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import im.status.keycard.android.NFCCardManager
import im.status.keycard.connect.card.CardScriptExecutor
import im.status.keycard.connect.card.ScriptListener
import im.status.keycard.connect.data.PINCache
import im.status.keycard.connect.data.PairingManager
import im.status.keycard.connect.data.SettingsManager
import im.status.keycard.connect.net.EthereumRPC
import im.status.keycard.connect.net.WalletConnect
import org.walletconnect.Session

@SuppressLint("StaticFieldLeak")
object Registry {
    lateinit var pinCache: PINCache
        private set

    lateinit var pairingManager: PairingManager
        private set

    lateinit var settingsManager: SettingsManager
        private set

    lateinit var mainActivity: Activity
        private set

    lateinit var scriptExecutor: CardScriptExecutor
        private set

    lateinit var cardManager: NFCCardManager
        private set

    lateinit var nfcAdapter: NfcAdapter
        private set

    lateinit var walletConnect: WalletConnect
        private set

    lateinit var ethereumRPC: EthereumRPC
        private set

    private fun moshiAddKotlin() {
        val factories = Moshi::class.java.getDeclaredField("BUILT_IN_FACTORIES")
        factories.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val value = factories.get(null) as java.util.ArrayList<Any>
        value.add(0, KotlinJsonAdapterFactory())
    }

    fun init(activity: Activity, listener: ScriptListener, sessionListener: Session.Callback) {
        //TODO: remove this hack, it is needed  now because KEthereum does not add the KotlinJsonAdapterFactory
        moshiAddKotlin()

        this.mainActivity = activity

        pairingManager = PairingManager(activity)
        pinCache = PINCache()
        settingsManager = SettingsManager(activity)

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        scriptExecutor = CardScriptExecutor(listener)

        cardManager = NFCCardManager()
        cardManager.setCardListener(scriptExecutor)
        cardManager.start()

        ethereumRPC = EthereumRPC(settingsManager.rpcEndpoint)
        walletConnect = WalletConnect(sessionListener, settingsManager.bip32Path, settingsManager.chainID)
    }
}