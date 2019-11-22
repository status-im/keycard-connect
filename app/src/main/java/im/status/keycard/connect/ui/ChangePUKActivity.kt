package im.status.keycard.connect.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ChangePUKCommand
import im.status.keycard.connect.card.scriptWithAuthentication

class ChangePUKActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: puk validation and confirmation
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_puk)
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
}
