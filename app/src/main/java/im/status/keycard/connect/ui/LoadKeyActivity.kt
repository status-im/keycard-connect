package im.status.keycard.connect.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.connect.R
import im.status.keycard.connect.data.*
import java.util.*

class LoadKeyActivity : NoNFCActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_key)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    fun generateBIP39(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent()
        intent.putExtra(LOAD_TYPE, LOAD_GENERATE_BIP39)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun generateOnCard(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent()
        intent.putExtra(LOAD_TYPE, LOAD_GENERATE)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun importBIP39(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent()
        intent.putExtra(LOAD_TYPE, LOAD_IMPORT_BIP39)
        intent.putExtra(LOAD_MNEMONIC, findViewById<EditText>(R.id.importMnemonicText).text.toString().toLowerCase(Locale.ENGLISH).trim())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
