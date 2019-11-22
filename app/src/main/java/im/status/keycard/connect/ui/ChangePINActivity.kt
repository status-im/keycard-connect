package im.status.keycard.connect.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ChangePINCommand
import im.status.keycard.connect.card.scriptWithAuthentication

class ChangePINActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: pin validation and confirmation
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val pinText = findViewById<EditText>(R.id.newPINText)
        val script = scriptWithAuthentication().plus(ChangePINCommand(pinText.text.toString()))
        Registry.scriptExecutor.runScript(script)
        finish()
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}
