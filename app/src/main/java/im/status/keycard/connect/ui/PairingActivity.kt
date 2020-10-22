package im.status.keycard.connect.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import im.status.keycard.connect.R
import im.status.keycard.connect.data.PAIRING_ACTIVITY_PASSWORD

class PairingActivity : NoNFCActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        val passText = findViewById<EditText>(R.id.passwordText)
        passText.doAfterTextChanged { findViewById<Button>(R.id.okButton).isEnabled = passText.text.toString().isNotEmpty() }
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent()
        intent.putExtra(PAIRING_ACTIVITY_PASSWORD, findViewById<EditText>(R.id.passwordText).text.toString())
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
