package im.status.keycard.connect.ui

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.android.NFCCardManager
import android.content.Intent
import im.status.keycard.connect.R
import im.status.keycard.connect.card.*
import im.status.keycard.connect.data.PairingManager
import im.status.keycard.connect.data.REQ_INTERACTIVE_SCRIPT

class MainActivity : AppCompatActivity() {
    private lateinit var cardManager: NFCCardManager
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var executor: CardScriptExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        PairingManager.init(this)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        executor = CardScriptExecutor(this)
        executor.setScript(listOf(SelectCommand(), InitCommand(), OpenSecureChannelCommand(), VerifyPINCommand()))

        cardManager = NFCCardManager()
        cardManager.setCardListener(executor)
        cardManager.start()
    }

    override fun onResume() {
        super.onResume()
        if (this::nfcAdapter.isInitialized) {
            nfcAdapter.enableReaderMode(this, this.cardManager,NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::nfcAdapter.isInitialized) {
            nfcAdapter.disableReaderMode(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_INTERACTIVE_SCRIPT) {
            executor.onUserInteractionReturned(resultCode, data)
        }
    }
}
