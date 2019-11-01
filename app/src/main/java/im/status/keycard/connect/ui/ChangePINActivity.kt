package im.status.keycard.connect.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import im.status.keycard.connect.R

class ChangePINActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: pin validation and confirmation
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)
    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {

        finish()
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}
