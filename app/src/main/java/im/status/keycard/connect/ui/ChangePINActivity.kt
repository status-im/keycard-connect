package im.status.keycard.connect.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ChangePINCommand
import im.status.keycard.connect.card.scriptWithAuthentication
import im.status.keycard.connect.data.isValidPIN

class ChangePINActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)

        val pinText = findViewById<EditText>(R.id.newPINText)
        val pinConfirmation = findViewById<EditText>(R.id.pinConfirmation)
        pinText.doAfterTextChanged { validatePIN() }
        pinConfirmation.doAfterTextChanged { validatePIN() }
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

    private fun validatePIN() {
        val pinText = findViewById<EditText>(R.id.newPINText).text.toString()
        val pinConfirmation = findViewById<EditText>(R.id.pinConfirmation).text.toString()
        val button = findViewById<Button>(R.id.okButton)
        button.isEnabled = (pinText == pinConfirmation) && isValidPIN(pinText)
    }
}
