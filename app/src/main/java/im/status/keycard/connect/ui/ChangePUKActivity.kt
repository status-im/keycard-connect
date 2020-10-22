package im.status.keycard.connect.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ChangePUKCommand
import im.status.keycard.connect.card.scriptWithAuthentication
import im.status.keycard.connect.data.isValidPUK

class ChangePUKActivity : NoNFCActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_puk)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        val pukText = findViewById<EditText>(R.id.newPUKText)
        val pukConfirmation = findViewById<EditText>(R.id.pukConfirmation)
        pukText.doAfterTextChanged { validatePUK() }
        pukConfirmation.doAfterTextChanged { validatePUK() }
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val pukText = findViewById<EditText>(R.id.newPUKText)
        val script = scriptWithAuthentication().plus(ChangePUKCommand(pukText.text.toString()))
        Registry.scriptExecutor.runScript(script)
        finish()
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }

    private fun validatePUK() {
        val pukText = findViewById<EditText>(R.id.newPUKText).text.toString()
        val pukConfirmation = findViewById<EditText>(R.id.pukConfirmation).text.toString()
        val button = findViewById<Button>(R.id.okButton)
        button.isEnabled = (pukText == pukConfirmation) && isValidPUK(pukText)
    }
}
