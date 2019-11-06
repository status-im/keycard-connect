package im.status.keycard.connect.walletconnect

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import im.status.keycard.applet.BIP32KeyPair
import im.status.keycard.applet.RecoverableSignature
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ExportKeyCommand
import im.status.keycard.connect.card.SignListener
import im.status.keycard.connect.card.SignMessageCommand
import im.status.keycard.connect.card.scriptWithAuthentication
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.encoders.Hex.toHexString
import org.walletconnect.Session
import org.walletconnect.Session.Config.Companion.fromWCUri
import org.walletconnect.impls.*
import java.io.File

class WalletConnect : ExportKeyCommand.Listener, SignListener {
    //TODO: Provide settings for these two
    private val bip39Path = "m/44'/60'/0'/0"
    private val chainID: Long = 1

    private val scope = MainScope()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val okHttpClient = OkHttpClient()
    private val sessionStore = FileWCSessionStore(File(Registry.mainActivity.filesDir, "wcSessions.json").apply { createNewFile() }, moshi)
    private var session: WCSession? = null
    private var requestId: Long = 0

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
                    is Session.MethodCall.SignMessage -> signText(call.id, call.message)
                    is Session.MethodCall.SendTransaction -> session?.rejectRequest(call.id, 1L, "Not implemented yet")
                    is Session.MethodCall.Custom -> onCustomCall(call)
                }
            }
        }

        private fun onCustomCall(call: Session.MethodCall.Custom) {
            when(call.method) {
                "personal_sign" -> {
                    val message = call.params?.first()

                    if (message is String) {
                        signText(call.id, message)
                    } else {
                        session?.rejectRequest(call.id, 1L, "Invalid params")
                    }
                }

                "eth_signTypedData" -> {
                    session?.rejectRequest(call.id, 1L, "Not implemented yet")
                }

                "eth_sendRawTransaction" -> {
                    session?.rejectRequest(call.id, 1L, "Not implemented yet")
                }

                else -> session?.rejectRequest(call.id, 1L, "Not implemented")
            }

        }

        private fun signText(id: Long, message: String) {
            requestId = id
            val msg = Hex.decode(if (message.startsWith("0x", true)) message.drop(2) else message)
            Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(SignMessageCommand(Registry.walletConnect, msg)))
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
            val addr = "0x${toHexString(keyPair.toEthereumAddress())}"
            session?.approve(listOf(addr), chainID)
        }
    }

    override fun onResponse(signature: RecoverableSignature?) {
        scope.launch {
            if (signature != null) {
                session?.approveRequest(requestId, "0x${toHexString(signature.r)}${toHexString(signature.s)}${toHexString(byteArrayOf(signature.recId.toByte()))}")
            } else {
                session?.rejectRequest(requestId, -1, "Rejected by user")
            }

        }
    }
}