package im.status.keycard.connect.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import im.status.keycard.globalplatform.Crypto
import android.util.Base64.NO_PADDING
import android.util.Base64.NO_WRAP
import android.widget.TextView
import im.status.keycard.connect.R


const val INIT_ACTIVITY_PIN = "initPIN"
const val INIT_ACTIVITY_PUK = "initPUK"
const val INIT_ACTIVITY_PAIRING = "initPairing"

class InitActivity : AppCompatActivity() {
    private lateinit var pin: String
    private lateinit var puk: String
    private lateinit var pairing: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        pin = String.format("%06d", Crypto.randomLong(Crypto.PIN_BOUND))
        puk = String.format("%012d", Crypto.randomLong(Crypto.PUK_BOUND));
        pairing = randomToken(6)

        findViewById<TextView>(R.id.pinView).text = pin
        findViewById<TextView>(R.id.pukView).text = puk
        findViewById<TextView>(R.id.pairingView).text = pairing
    }

    fun ok(view: View) {
        val intent = Intent()
        intent.putExtra(INIT_ACTIVITY_PIN, pin)
        intent.putExtra(INIT_ACTIVITY_PUK, puk)
        intent.putExtra(INIT_ACTIVITY_PAIRING, pairing)

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun cancel(view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    fun randomToken(length: Int): String {
        return Base64.encodeToString(Crypto.randomBytes(length), NO_PADDING or NO_WRAP)
    }
}
