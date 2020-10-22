package im.status.keycard.connect.ui

import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.connect.Registry


open class NoNFCActivity : AppCompatActivity(), ReaderCallback {
    override fun onResume() {
        super.onResume()
        Registry.nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
    }

    override fun onPause() {
        super.onPause()
        Registry.nfcAdapter.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {}
}