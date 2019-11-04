package im.status.keycard.connect.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.data.PIN_ACTIVITY_ATTEMPTS
import im.status.keycard.connect.data.PIN_ACTIVITY_CARD_UID

class PINActivity : AppCompatActivity() {
    private lateinit var cardUID: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: validate PIN length == 6

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        val attempts = intent.getIntExtra(PIN_ACTIVITY_ATTEMPTS, -1)
        cardUID = intent.getByteArrayExtra(PIN_ACTIVITY_CARD_UID)!!

        val attemptLabel = findViewById<TextView>(R.id.attemptLabel)

        if (attempts == -1) {
            attemptLabel.text = ""
        } else {
            attemptLabel.text = getString(R.string.pin_attempts, attempts)
        }
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
}
