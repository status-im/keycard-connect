package im.status.keycard.connect.walletconnect

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import im.status.keycard.applet.BIP32KeyPair
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ExportKeyCommand
import im.status.keycard.connect.card.scriptWithAuthentication
import im.status.keycard.connect.card.toHexString
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.walletconnect.Session
import org.walletconnect.Session.Config.Companion.fromWCUri
import org.walletconnect.impls.*
import java.io.File

class SessionManager : ExportKeyCommand.Listener {
    //TODO: Provide settings for these two
    private val bip39Path = "m/44'/60'/0'/0"
    private val chainID: Long = 1

    private val scope = MainScope()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val okHttpClient = OkHttpClient()
    private val sessionStore = FileWCSessionStore(File(Registry.mainActivity.filesDir, "wcSessions.json").apply { createNewFile() }, moshi)
    private var session: WCSession? = null

    private val sessionCB = object : Session.Callback {
        override fun onStatus(status: Session.Status) {
            when (status) {
                is Session.Status.Error -> println("WalletConnect Error")
                is Session.Status.Approved -> println("WalletConnect Approved")
                is Session.Status.Connected -> println("WalletConnect Connected")
                is Session.Status.Disconnected -> println("WalletConnect Disconnected")
                is Session.Status.Closed -> session = null
            }
        }

        override fun onMethodCall(call: Session.MethodCall) {
            scope.launch {
                when (call) {
                    is Session.MethodCall.SessionRequest -> Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(ExportKeyCommand(Registry.walletConnect, bip39Path)))
                    is Session.MethodCall.SignMessage -> session?.rejectRequest(call.id, 1L, "Not implemented yet")
                    is Session.MethodCall.SendTransaction -> session?.rejectRequest(call.id, 1L, "Not implemented yet")
                    is Session.MethodCall.Custom -> session?.rejectRequest(call.id, 1L, "Not implemented yet")
                }
            }
        }
    }

    fun connect(uri: String) {
        scope.launch {
            session?.kill()

            session = WCSession(
                fromWCUri(uri),
                MoshiPayloadAdapter(moshi),
                sessionStore,
                OkHttpTransport.Builder(okHttpClient, moshi),
                Session.PeerMeta(name = "Keycard Connect")
            )

            session?.addCallback(sessionCB)
            session?.init()
        }
    }

    override fun onResponse(keyPair: BIP32KeyPair) {
        scope.launch {
            val addr = "0x${keyPair.toEthereumAddress().toHexString()}"
            session?.approve(listOf(addr), chainID)
        }
    }
}