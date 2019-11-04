package im.status.keycard.connect.ui

import android.app.Activity
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.ViewSwitcher
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.*
import im.status.keycard.connect.data.REQ_INTERACTIVE_SCRIPT
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

        if (requestCode == REQ_INTERACTIVE_SCRIPT) {
            Registry.scriptExecutor.onUserInteractionReturned(resultCode, data)
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
        }
    }

    fun cancelNFC(@Suppress("UNUSED_PARAMETER") view: View) {
        Registry.scriptExecutor.cancelScript()
    }

    fun changePIN(@Suppress("UNUSED_PARAMETER") view: View) {
        startCommand(ChangePINActivity::class)
    }

    private fun startCommand(activity: KClass<out Activity>) {
        val intent = Intent(this, activity.java)
        startActivity(intent)
    }
}
