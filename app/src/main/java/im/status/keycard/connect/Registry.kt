package im.status.keycard.connect

import android.annotation.SuppressLint
import android.app.Activity
import android.nfc.NfcAdapter
import im.status.keycard.android.NFCCardManager
import im.status.keycard.connect.card.*
import im.status.keycard.connect.data.PINCache
import im.status.keycard.connect.data.PairingManager
import im.status.keycard.connect.walletconnect.SessionManager

@SuppressLint("StaticFieldLeak")
object Registry {
    lateinit var pinCache: PINCache
        private set

    lateinit var pairingManager: PairingManager
        private set

    lateinit var mainActivity: Activity
        private set

    lateinit var scriptExecutor: CardScriptExecutor
        private set

    lateinit var cardManager: NFCCardManager
        private set

    lateinit var nfcAdapter: NfcAdapter
        private set

    lateinit var walletConnect: SessionManager

    fun init(activity: Activity, listener: ScriptListener) {
        this.mainActivity = activity

        pairingManager = PairingManager(activity)
        pinCache = PINCache()

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        scriptExecutor = CardScriptExecutor(listener)

        cardManager = NFCCardManager()
        cardManager.setCardListener(scriptExecutor)
        cardManager.start()

        walletConnect = SessionManager()
    }
}