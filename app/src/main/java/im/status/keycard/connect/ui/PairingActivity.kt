package im.status.keycard.connect.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.content.Intent
import im.status.keycard.connect.R
import im.status.keycard.connect.data.PAIRING_ACTIVITY_PASSWORD

class PairingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)
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
}
