package im.status.keycard.connect.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import im.status.keycard.connect.R
import im.status.keycard.connect.data.SIGN_TEXT_MESSAGE

class SignMessageActivity : NoNFCActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_message)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        val signText = findViewById<TextView>(R.id.signText)
        signText.text = intent.getStringExtra(SIGN_TEXT_MESSAGE)
    }

    fun sign(@Suppress("UNUSED_PARAMETER") view: View) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun reject(@Suppress("UNUSED_PARAMETER") view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
