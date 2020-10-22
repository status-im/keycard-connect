package im.status.keycard.connect.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.data.PUK_ACTIVITY_ATTEMPTS
import im.status.keycard.connect.data.isValidPIN
import im.status.keycard.connect.data.isValidPUK

class PUKActivity : NoNFCActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puk)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        val attempts = intent.getIntExtra(PUK_ACTIVITY_ATTEMPTS, -1)

        val attemptLabel = findViewById<TextView>(R.id.attemptLabel)

        if (attempts == -1) {
            attemptLabel.text = ""
        } else {
            attemptLabel.text = getString(R.string.pin_attempts, attempts)
        }

        findViewById<EditText>(R.id.pukText).doAfterTextChanged { validateFields() }
        findViewById<EditText>(R.id.newPINText).doAfterTextChanged { validateFields() }
        findViewById<EditText>(R.id.pinConfirmation).doAfterTextChanged { validateFields() }
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val pukText = findViewById<EditText>(R.id.pukText)
        val pinText = findViewById<EditText>(R.id.newPINText)

        Registry.pinCache.pukAndPIN = Pair(pukText.text.toString(), pinText.text.toString())
        setResult(Activity.RESULT_OK)
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

    private fun validateFields() {
        val pukText = findViewById<EditText>(R.id.pukText).text.toString()
        val pinText = findViewById<EditText>(R.id.newPINText).text.toString()
        val pinConfirmationText = findViewById<EditText>(R.id.pinConfirmation).text.toString()
        val button = findViewById<Button>(R.id.okButton)
        button.isEnabled = (pinText == pinConfirmationText) && isValidPIN(pinText) && isValidPUK(pukText)
    }

}
