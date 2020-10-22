package im.status.keycard.connect.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ReinstallCommand
import im.status.keycard.connect.data.REQ_APPLET_FILE

class ReinstallActivity : NoNFCActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reinstall)
    }

    fun reinstall(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(intent, REQ_APPLET_FILE)
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_APPLET_FILE && resultCode == RESULT_OK) {
            val reinstallWallet = findViewById<CheckBox>(R.id.reinstallWalletCheckbox).isChecked
            val reinstallCash = findViewById<CheckBox>(R.id.reinstallCashCheckbox).isChecked
            val reinstallNDEF = findViewById<CheckBox>(R.id.reinstallNDEFCheckbox).isChecked
            data?.data?.also { uri ->
                val script = listOf(ReinstallCommand(uri, reinstallWallet, reinstallCash, reinstallNDEF))
                Registry.scriptExecutor.runScript(script)
            }
        }

        finish()
    }
}
