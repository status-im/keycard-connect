package im.status.keycard.connect

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import im.status.keycard.connect.data.PINCache
import android.content.Intent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService

const val PAIRING_ACTIVITY_PASSWORD = "pairingPassword"

class PairingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)
    }

    fun ok(view: View) {
        val intent = Intent()
        intent.putExtra(PAIRING_ACTIVITY_PASSWORD, findViewById<EditText>(R.id.passwordText).text.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun cancel(view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
