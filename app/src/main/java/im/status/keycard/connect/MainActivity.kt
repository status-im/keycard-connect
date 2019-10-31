package im.status.keycard.connect

import android.nfc.NfcAdapter
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import im.status.keycard.android.NFCCardManager
import android.content.Intent
import im.status.keycard.connect.card.*

class MainActivity : AppCompatActivity() {
    private lateinit var cardManager: NFCCardManager
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var executor: CardScriptExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        executor = CardScriptExecutor(this)
        executor.setScript(listOf(SelectCommand(), InitCommand(), OpenSecureChannelCommand(), VerifyPINCommand()))

        cardManager = NFCCardManager()
        cardManager.setCardListener(executor)
        cardManager.start()

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        if (this::nfcAdapter.isInitialized) {
            nfcAdapter.enableReaderMode(this, this.cardManager,NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::nfcAdapter.isInitialized) {
            nfcAdapter.disableReaderMode(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_INTERACTIVE_SCRIPT) {
            executor.onUserInteractionReturned(resultCode, data)
        }
    }
}
