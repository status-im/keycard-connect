package im.status.keycard.connect

import android.annotation.SuppressLint
import android.app.Activity
import android.nfc.NfcAdapter
import im.status.keycard.android.NFCCardManager
import im.status.keycard.connect.card.*
import im.status.keycard.connect.data.PINCache
import im.status.keycard.connect.data.PairingManager

object Registry {
    lateinit var pinCache: PINCache
        private set

    lateinit var pairingManager: PairingManager
        private set

    @SuppressLint("StaticFieldLeak")
    lateinit var scriptExecutor: CardScriptExecutor
        private set

    lateinit var cardManager: NFCCardManager
        private set

    lateinit var nfcAdapter: NfcAdapter
        private set

    fun init(activity: Activity) {
        pairingManager = PairingManager(activity)
        pinCache = PINCache()

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        scriptExecutor = CardScriptExecutor(activity)

        cardManager = NFCCardManager()
        cardManager.setCardListener(scriptExecutor)
        cardManager.start()
    }
}