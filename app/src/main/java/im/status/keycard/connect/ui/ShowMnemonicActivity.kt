package im.status.keycard.connect.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.connect.R
import im.status.keycard.connect.data.MNEMONIC_PHRASE

class ShowMnemonicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_mnemonic)

        val mnemonic = intent.getStringArrayExtra(MNEMONIC_PHRASE)
        val mnemonicView = findViewById<TextView>(R.id.mnemonicView)
        mnemonicView.text = mnemonic?.joinToString()
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}
