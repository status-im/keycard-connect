package im.status.keycard.connect.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import im.status.keycard.connect.R
import im.status.keycard.connect.data.MNEMONIC_PHRASE

class ShowMnemonicActivity : NoNFCActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_mnemonic)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        val mnemonic = intent.getStringArrayExtra(MNEMONIC_PHRASE)
        val mnemonicView = findViewById<TextView>(R.id.mnemonicView)
        mnemonicView.text = mnemonic?.joinToString()
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}
