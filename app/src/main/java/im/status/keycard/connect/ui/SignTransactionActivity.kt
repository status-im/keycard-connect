package im.status.keycard.connect.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import im.status.keycard.connect.R
import im.status.keycard.connect.data.SIGN_TX_AMOUNT
import im.status.keycard.connect.data.SIGN_TX_CURRENCY
import im.status.keycard.connect.data.SIGN_TX_DATA
import im.status.keycard.connect.data.SIGN_TX_TO

class SignTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_transaction)

        findViewById<TextView>(R.id.signTxAmount).text = intent.getStringExtra(SIGN_TX_AMOUNT)
        findViewById<TextView>(R.id.signTxCurrency).text = intent.getStringExtra(SIGN_TX_CURRENCY)
        findViewById<TextView>(R.id.signTxTo).text = intent.getStringExtra(SIGN_TX_TO)
        findViewById<TextView>(R.id.signTxData).text = intent.getStringExtra(SIGN_TX_DATA)
    }

    fun sign(@Suppress("UNUSED_PARAMETER") view: View) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun reject(@Suppress("UNUSED_PARAMETER") view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
