package im.status.keycard.connect.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.data.PIN_ACTIVITY_ATTEMPTS
import im.status.keycard.connect.data.PIN_ACTIVITY_CARD_UID
import im.status.keycard.connect.data.isValidPIN

class PINActivity : NoNFCActivity() {
    private lateinit var cardUID: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        val attempts = intent.getIntExtra(PIN_ACTIVITY_ATTEMPTS, -1)
        cardUID = intent.getByteArrayExtra(PIN_ACTIVITY_CARD_UID)!!

        val attemptLabel = findViewById<TextView>(R.id.attemptLabel)

        if (attempts == -1) {
            attemptLabel.text = ""
        } else {
            attemptLabel.text = getString(R.string.pin_attempts, attempts)
        }

        val pinText = findViewById<EditText>(R.id.pinText)
        pinText.doAfterTextChanged { findViewById<Button>(R.id.okButton).isEnabled = isValidPIN(pinText.text.toString()) }
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val pinText = findViewById<EditText>(R.id.pinText)

        Registry.pinCache.putPIN(cardUID, pinText.text.toString())
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
}
