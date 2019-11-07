package im.status.keycard.connect.walletconnect

import android.app.Activity
import android.content.Intent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import im.status.keycard.applet.BIP32KeyPair
import im.status.keycard.applet.RecoverableSignature
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ExportKeyCommand
import im.status.keycard.connect.card.SignCommand
import im.status.keycard.connect.card.scriptWithAuthentication
import im.status.keycard.connect.data.REQ_WALLETCONNECT
import im.status.keycard.connect.data.SIGN_TEXT_MESSAGE
import im.status.keycard.connect.ui.SignMessageActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.walletconnect.Session
import org.walletconnect.Session.Config.Companion.fromWCUri
import org.walletconnect.impls.*
import org.walleth.khex.hexToByteArray
import org.walleth.khex.toHexString
import org.walleth.khex.toNoPrefixHexString
import java.io.File

class WalletConnect : ExportKeyCommand.Listener, SignCommand.Listener {
    //TODO: Provide settings for these two
    private val bip39Path = "m/44'/60'/0'/0"
    private val chainID: Long = 1

    private val scope = MainScope()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val okHttpClient = OkHttpClient()
    private val sessionStore = FileWCSessionStore(File(Registry.mainActivity.filesDir, "wcSessions.json").apply { createNewFile() }, moshi)
    private var session: WCSession? = null
    private var requestId: Long = 0
    private var action: (data: Intent?) -> Unit = this::nop

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
                    val message = call.params?.get(1)

                    if (message is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        signTypedData(call.id, message as Map<String, String>)
                    } else {
                        session?.rejectRequest(call.id, 1L, "Invalid params")
                    }
                }

                "eth_signTransaction" -> {
                    session?.rejectRequest(call.id, 1L, "Not implemented yet")
                }

                "eth_sendRawTransaction" -> {
                    val signedTx = call.params?.first()

                    if (signedTx is String) {
                        relayTX(call.id, signedTx)
                    } else {
                        session?.rejectRequest(call.id, 1L, "Invalid params")
                    }
                }

                else -> session?.rejectRequest(call.id, 1L, "Not implemented")
            }

        }

        private fun relayTX(id: Long, signedTx: Any) {
            // Ask confirmation and forward tx as-is through Infura
            println(signedTx)
            session?.rejectRequest(id, 1L, "Not implemented yet")
        }

        private fun signText(id: Long, message: String) {
            val msg = message.hexToByteArray()
            val text = String(msg)

            requestId = id
            action = {
                val keccak256 = Keccak.Digest256()
                val hash = keccak256.digest(byteArrayOf(0x19) + "Ethereum Signed Message:\n${msg.size}".toByteArray() + msg)
                Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(SignCommand(Registry.walletConnect, hash)))
            }

            val intent = Intent(Registry.mainActivity, SignMessageActivity::class.java).apply {
                putExtra(SIGN_TEXT_MESSAGE, text)
            }

            Registry.mainActivity.startActivityForResult(intent, REQ_WALLETCONNECT)
        }

        private fun signTypedData(id: Long, message: Map<String, String>) {
            session?.rejectRequest(id, 1L, "Not implemented yet")
        }
    }

    private fun nop(@Suppress("UNUSED_PARAMETER") data: Intent?) { }

    fun onUserInteractionReturned(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            action.invoke(data)
        } else {
            session?.rejectRequest(requestId, -1, "Rejected by user")
        }

        action = this::nop
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
            val addr = keyPair.toEthereumAddress().toHexString()
            session?.approve(listOf(addr), chainID)
        }
    }

    override fun onResponse(signature: RecoverableSignature) {
        scope.launch {
            session?.approveRequest(requestId, "0x${signature.r.toNoPrefixHexString()}${signature.s.toNoPrefixHexString()}${(signature.recId + 27).toByte().toHexString()}")
        }
    }
}