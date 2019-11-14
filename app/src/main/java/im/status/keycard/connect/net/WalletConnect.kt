package im.status.keycard.connect.net

import android.app.Activity
import android.content.Intent
import com.squareup.moshi.Moshi
import im.status.keycard.applet.BIP32KeyPair
import im.status.keycard.applet.RecoverableSignature
import im.status.keycard.connect.Registry
import im.status.keycard.connect.card.ExportKeyCommand
import im.status.keycard.connect.card.SignCommand
import im.status.keycard.connect.card.scriptWithAuthentication
import im.status.keycard.connect.data.*
import im.status.keycard.connect.ui.SignMessageActivity
import im.status.keycard.connect.ui.SignTransactionActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.kethereum.DEFAULT_GAS_LIMIT
import org.kethereum.extensions.maybeHexToBigInteger
import org.kethereum.extensions.toBigInteger
import org.kethereum.functions.encodeRLP
import org.kethereum.functions.getTokenTransferTo
import org.kethereum.functions.getTokenTransferValue
import org.kethereum.functions.isTokenTransfer
import org.kethereum.keccakshortcut.keccak
import org.kethereum.model.*
import org.walletconnect.Session
import org.walletconnect.Session.Config.Companion.fromWCUri
import org.walletconnect.impls.*
import org.walleth.khex.hexToByteArray
import org.walleth.khex.toHexString
import org.walleth.khex.toNoPrefixHexString
import java.io.File
import java.lang.Exception
import java.math.BigInteger

class WalletConnect(var bip32Path: String, var chainID: Long) : ExportKeyCommand.Listener, SignCommand.Listener, Session.Callback {

    private val scope = MainScope()
    private val moshi = Moshi.Builder().build()
    private val okHttpClient = OkHttpClient()
    private val sessionStore = FileWCSessionStore(File(Registry.mainActivity.filesDir, "wcSessions.json").apply { createNewFile() }, moshi)
    private var session: WCSession? = null
    private var requestId: Long = 0
    private var uiAction: (Intent?) -> Unit = this::nop
    private var signAction: (RecoverableSignature) -> Unit = this::nop

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
        scope.launch(Dispatchers.IO) {
            when (call) {
                is Session.MethodCall.SessionRequest -> Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(ExportKeyCommand(Registry.walletConnect, bip32Path)))
                is Session.MethodCall.SignMessage -> signText(call.id, call.message)
                is Session.MethodCall.SendTransaction -> signTransaction(call.id, toTransaction(call), true)
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
        val gasLimit = tx.gasLimit?.maybeHexToBigInteger() ?: DEFAULT_GAS_LIMIT
        val gasPrice = tx.gasPrice?.maybeHexToBigInteger() ?: Registry.ethereumRPC.ethGasPrice()
        val nonce = tx.nonce?.maybeHexToBigInteger() ?: Registry.ethereumRPC.ethGetTransactionCount(tx.from)

        return Transaction(chainID.toBigInteger(), null, Address(tx.from), gasLimit, gasPrice, tx.data.hexToByteArray(), nonce, Address(tx.to), null, tx.value.maybeHexToBigInteger())
    }

    private fun relayTX(id: Long, signedTx: String) {
        session?.approveRequest(id, Registry.ethereumRPC.ethSendRawTransaction(signedTx))
    }

    private fun signText(id: Long, message: String) {
        val msg = message.hexToByteArray()
        val text = String(msg)

        requestId = id
        uiAction = {
            val hash = (byteArrayOf(0x19) + "Ethereum Signed Message:\n${msg.size}".toByteArray() + msg).keccak()
            Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(SignCommand(Registry.walletConnect, hash)))
        }

        signAction = { session?.approveRequest(requestId, "0x${it.r.toNoPrefixHexString()}${it.s.toNoPrefixHexString()}${(it.recId + 27).toByte().toHexString()}") }

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

        uiAction = {
            val hash = tx.encodeRLP(SignatureData(v = chainID.toBigInteger())).keccak()
            Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(SignCommand(Registry.walletConnect, hash)))
        }

        signAction = {
            try {
                val signedTx = SignedTransaction(tx, SignatureData(it.r.toBigInteger(), it.s.toBigInteger(), (it.recId + (chainID * 2) + 35).toBigInteger())).encodeRLP().toHexString()
                val res = if (send) Registry.ethereumRPC.ethSendRawTransaction(signedTx) else signedTx
                session?.approveRequest(requestId, res)
            } catch(e: Exception) {
                session?.rejectRequest(requestId, 1L, "Internal error")
            }
        }

        val intent = Intent(Registry.mainActivity, SignTransactionActivity::class.java).apply {
            if (tx.isTokenTransfer()) {
                putExtra(SIGN_TX_AMOUNT, tx.getTokenTransferValue().toString(10))
                //TODO: Replace with short name
                putExtra(SIGN_TX_CURRENCY, tx.to?.hex)
                putExtra(SIGN_TX_TO, tx.getTokenTransferTo().hex)
            } else {
                putExtra(SIGN_TX_AMOUNT, tx.value?.toString(10))
                putExtra(SIGN_TX_CURRENCY, "ETH")
                putExtra(SIGN_TX_TO, tx.to?.hex)
                putExtra(SIGN_TX_DATA, tx.input.toNoPrefixHexString())
            }
        }

        Registry.mainActivity.startActivityForResult(intent, REQ_WALLETCONNECT)
    }

    private fun nop(@Suppress("UNUSED_PARAMETER") ignored: Any?) { }

    fun onUserInteractionReturned(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            uiAction(data)
        } else {
            session?.rejectRequest(requestId, -1, "Rejected by user")
        }

        uiAction = this::nop
    }

    fun connect(uri: String) {
        scope.launch(Dispatchers.IO) {
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
        scope.launch(Dispatchers.IO) {
            val addr = keyPair.toEthereumAddress().toHexString()
            session?.approve(listOf(addr), chainID)
        }
    }

    override fun onResponse(signature: RecoverableSignature) {
        scope.launch(Dispatchers.IO) {
            signAction(signature)
            signAction = Registry.walletConnect::nop
        }
    }
}