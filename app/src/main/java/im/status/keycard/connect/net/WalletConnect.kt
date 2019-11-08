package im.status.keycard.connect.net

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
import org.kethereum.model.Transaction
import org.kethereum.model.createEmptyTransaction
import org.walletconnect.Session
import org.walletconnect.Session.Config.Companion.fromWCUri
import org.walletconnect.impls.*
import org.walleth.khex.hexToByteArray
import org.walleth.khex.toHexString
import org.walleth.khex.toNoPrefixHexString
import java.io.File
import java.lang.Exception

class WalletConnect : ExportKeyCommand.Listener, SignCommand.Listener, Session.Callback {
    //TODO: Provide settings for these two
    private val bip39Path = "m/44'/60'/0'/0"
    private val chainID: Long = 1

    private val scope = MainScope()
    private val moshi = Moshi.Builder().build()
    private val okHttpClient = OkHttpClient()
    private val sessionStore = FileWCSessionStore(File(Registry.mainActivity.filesDir, "wcSessions.json").apply { createNewFile() }, moshi)
    private var session: WCSession? = null
    private var requestId: Long = 0
    private var action: (data: Intent?) -> Unit = this::nop

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
                is Session.MethodCall.SendTransaction -> signTransaction(call.id, toTransaction(call), false)
                is Session.MethodCall.Custom -> onCustomCall(call)
            }
        }
    }

    private inline fun <reified T> runOnValidParam(call: Session.MethodCall.Custom, index: Int, body: (T) -> Unit) {
        val param = call.params?.getOrNull(index)

        if (param is T) {
            try {
                body(param)
            } catch(e: Exception) {
                session?.rejectRequest(call.id, 1L, "Internal error")
            }
        } else {
            session?.rejectRequest(call.id, 1L, "Invalid params")
        }
    }

    private fun onCustomCall(call: Session.MethodCall.Custom) {
        when(call.method) {
            "personal_sign" -> runOnValidParam<String>(call, 0) { signText(call.id, it) }
            "eth_signTypedData" -> { runOnValidParam<Map<*, *>>(call, 1) { @Suppress("UNCHECKED_CAST") signTypedData(call.id, it as Map<String, String>) } }
            "eth_signTransaction" -> { runOnValidParam<Map<*, *>>(call, 0) { signTransaction(call.id, toTransaction(toSendTransaction(call.id, it)), false)} }
            "eth_sendRawTransaction" -> { runOnValidParam<String>(call, 0) { relayTX(call.id, it) } }
            else -> session?.rejectRequest(call.id, 1L, "Not implemented")
        }
    }

    private fun toSendTransaction(id: Long, data: Map<*, *>): Session.MethodCall.SendTransaction {
        val from = data["from"] as? String ?: throw IllegalArgumentException("from key missing")
        val to = data["to"] as? String ?: throw IllegalArgumentException("to key missing")
        val nonce = data["nonce"] as? String ?: (data["nonce"] as? Double)?.toLong()?.toString()
        val gasPrice = data["gasPrice"] as? String
        val gasLimit = data["gasLimit"] as? String
        val value = data["value"] as? String ?: throw IllegalArgumentException("value key missing")
        val txData = data["data"] as? String ?: throw IllegalArgumentException("data key missing")
        return Session.MethodCall.SendTransaction(id, from, to, nonce, gasPrice, gasLimit, value, txData)
    }

    private fun toTransaction(tx: Session.MethodCall.SendTransaction): Transaction {
        return createEmptyTransaction()
    }

    private fun relayTX(id: Long, signedTx: String) {
        session?.approveRequest(id, Registry.ethereumRPC.ethSendRawTransaction(signedTx))
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
        requestId = id
        session?.rejectRequest(id, 1L, "Not implemented yet")
    }

    private fun signTransaction(id: Long, tx: Transaction, send: Boolean) {
        requestId = id
        session?.rejectRequest(id, 1L, "Not implemented yet")
    }

    private fun nop(@Suppress("UNUSED_PARAMETER") data: Intent?) { }

    fun onUserInteractionReturned(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            action(data)
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

            session?.addCallback(Registry.walletConnect)
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