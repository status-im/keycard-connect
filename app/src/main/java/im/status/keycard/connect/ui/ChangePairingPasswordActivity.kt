package im.status.keycard.connect.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ChangePairingPasswordCommand
import im.status.keycard.connect.card.scriptWithAuthentication
import im.status.keycard.connect.data.isValidPUK

class ChangePairingPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pairing_password)

        val pairingPasswordText = findViewById<EditText>(R.id.newPairingPasswordText)
        val pairingPasswordConfirmation = findViewById<EditText>(R.id.pairingPasswordConfirmation)
        pairingPasswordText.doAfterTextChanged { validatePairingPassword() }
        pairingPasswordConfirmation.doAfterTextChanged { validatePairingPassword() }
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val pairingPasswordText = findViewById<EditText>(R.id.newPairingPasswordText)
        val script = scriptWithAuthentication().plus(ChangePairingPasswordCommand(pairingPasswordText.text.toString()))
        Registry.scriptExecutor.runScript(script)
        finish()
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }

    private fun validatePairingPassword() {
        val pairingPasswordText = findViewById<EditText>(R.id.newPairingPasswordText).text.toString()
        val pairingPasswordConfirmation = findViewById<EditText>(R.id.pairingPasswordConfirmation).text.toString()
        val button = findViewById<Button>(R.id.okButton)
        button.isEnabled = (pairingPasswordText == pairingPasswordConfirmation) && pairingPasswordText.isNotEmpty()
    }
}
