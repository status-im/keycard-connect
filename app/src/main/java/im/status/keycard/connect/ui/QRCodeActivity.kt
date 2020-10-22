package im.status.keycard.connect.ui

import android.nfc.NfcAdapter
import android.nfc.Tag
import com.journeyapps.barcodescanner.CaptureActivity
import im.status.keycard.connect.Registry

class QRCodeActivity : CaptureActivity(), NfcAdapter.ReaderCallback {
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