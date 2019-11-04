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
import im.status.keycard.connect.data.PUK_ACTIVITY_ATTEMPTS

class PUKActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: validate PUK length == 12
        //TODO: validate PIN length == 6

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puk)
        val attempts = intent.getIntExtra(PUK_ACTIVITY_ATTEMPTS, -1)

        val attemptLabel = findViewById<TextView>(R.id.attemptLabel)

        if (attempts == -1) {
            attemptLabel.text = ""
        } else {
            attemptLabel.text = getString(R.string.pin_attempts, attempts)
        }
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
}
