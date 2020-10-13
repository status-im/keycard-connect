package im.status.keycard.connect.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Base64.NO_PADDING
import android.util.Base64.NO_WRAP
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.connect.R
import im.status.keycard.connect.data.INIT_ACTIVITY_PAIRING
import im.status.keycard.connect.data.INIT_ACTIVITY_PIN
import im.status.keycard.connect.data.INIT_ACTIVITY_PUK
import im.status.keycard.globalplatform.Crypto

class InitActivity : AppCompatActivity() {
    private lateinit var pin: String
    private lateinit var puk: String
    private lateinit var pairing: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        pin = String.format("%06d", Crypto.randomLong(Crypto.PIN_BOUND))
        puk = String.format("%012d", Crypto.randomLong(Crypto.PUK_BOUND))
        pairing = randomToken(6)

        findViewById<TextView>(R.id.pinView).text = pin
        findViewById<TextView>(R.id.pukView).text = puk
        findViewById<TextView>(R.id.pairingView).text = pairing
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent()
        intent.putExtra(INIT_ACTIVITY_PIN, pin)
        intent.putExtra(INIT_ACTIVITY_PUK, puk)
        intent.putExtra(INIT_ACTIVITY_PAIRING, pairing)

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

    private fun randomToken(length: Int): String {
        return Base64.encodeToString(Crypto.randomBytes(length), NO_PADDING or NO_WRAP)
    }
}
