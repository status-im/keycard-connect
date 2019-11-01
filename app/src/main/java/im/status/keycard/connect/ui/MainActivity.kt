package im.status.keycard.connect.ui

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.android.NFCCardManager
import android.content.Intent
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.*
import im.status.keycard.connect.data.PairingManager
import im.status.keycard.connect.data.REQ_INTERACTIVE_SCRIPT

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Registry.init(this)
        Registry.scriptExecutor.setScript(listOf(SelectCommand(), InitCommand(), OpenSecureChannelCommand(), VerifyPINCommand()))
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

        if (requestCode == REQ_INTERACTIVE_SCRIPT) {
            Registry.scriptExecutor.onUserInteractionReturned(resultCode, data)
        }
    }
}
