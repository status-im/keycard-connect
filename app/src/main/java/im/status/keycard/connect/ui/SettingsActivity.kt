package im.status.keycard.connect.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import im.status.keycard.connect.R
import im.status.keycard.connect.Registry
import im.status.keycard.connect.data.CHAIN_IDS

class SettingsActivity : AppCompatActivity() {
    lateinit var networkSpinner : Spinner
    lateinit var walletPath : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        networkSpinner = findViewById(R.id.networkSpinner)
        ArrayAdapter.createFromResource(this, R.array.networks, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            networkSpinner.adapter = it
        }
        networkSpinner.setSelection(CHAIN_IDS.indexOf(Registry.settingsManager.chainID))

        walletPath = findViewById(R.id.walletPathText)
        walletPath.setText(Registry.settingsManager.bip32Path)

    }

    fun ok(@Suppress("UNUSED_PARAMETER") view: View) {
        val chainID = CHAIN_IDS[networkSpinner.selectedItemPosition]
        Registry.settingsManager.chainID = chainID
        Registry.ethereumRPC.changeEndpoint(Registry.settingsManager.rpcEndpoint)

        val bip32Path = walletPath.text.toString()
        Registry.settingsManager.bip32Path = bip32Path

        Registry.walletConnect.updateChainAndDerivation(bip32Path, chainID)
        finish()
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}
