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
import org.kethereum.extensions.transactions.encodeRLP
import org.kethereum.extensions.transactions.getTokenTransferTo
import org.kethereum.extensions.transactions.getTokenTransferValue
import org.kethereum.extensions.transactions.isTokenTransfer
import org.kethereum.keccakshortcut.keccak
import org.kethereum.model.Address
import org.kethereum.model.SignatureData
import org.kethereum.model.SignedTransaction
import org.kethereum.model.Transaction
import org.komputing.khex.encode
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.extensions.toHexString
import org.komputing.khex.extensions.toNoPrefixHexString
import org.komputing.khex.model.HexString
import org.walletconnect.Session
import org.walletconnect.Session.Config.Companion.fromWCUri
import org.walletconnect.impls.FileWCSessionStore
import org.walletconnect.impls.MoshiPayloadAdapter
import org.walletconnect.impls.OkHttpTransport
import org.walletconnect.impls.WCSession
import pm.gnosis.eip712.EIP712JsonParser
import pm.gnosis.eip712.adapters.moshi.MoshiAdapter
import pm.gnosis.eip712.typedDataHash
import java.io.File
import kotlin.reflect.typeOf

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
                else -> session?.rejectRequest(call.id(), 1L, "Not implemented")
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
            "eth_signTypedData" -> { runOnValidParam<String>(call, 1) { @Suppress("UNCHECKED_CAST") signTypedData(call.id, it) } }
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
        val gasLimit = if(tx.gasLimit != null) HexString(tx.gasLimit!!).maybeHexToBigInteger() else DEFAULT_GAS_LIMIT
        val gasPrice = if(tx.gasPrice != null) HexString(tx.gasPrice!!).maybeHexToBigInteger() else Registry.ethereumRPC.ethGasPrice()
        val nonce = if(tx.nonce != null) HexString(tx.nonce!!).maybeHexToBigInteger() else Registry.ethereumRPC.ethGetTransactionCount(tx.from)
        val to = if(tx.to != null) Address(tx.to!!) else null
        return Transaction(chainID.toBigInteger(), null, Address(tx.from), gasLimit, gasPrice, HexString(tx.data).hexToByteArray(), nonce, to, null, HexString(tx.value).maybeHexToBigInteger(),null, null)
    }

    private fun relayTX(id: Long, signedTx: String) {
        session?.approveRequest(id, Registry.ethereumRPC.ethSendRawTransaction(signedTx))
    }

    private fun signText(id: Long, message: String) {
        val msg = HexString(message).hexToByteArray()
        val text = String(msg)

        requestId = id
        uiAction = {
            val hash = (byteArrayOf(0x19) + "Ethereum Signed Message:\n${msg.size}".toByteArray() + msg).keccak()
            Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(SignCommand(Registry.walletConnect, hash)))
        }

        signAction = { session?.approveRequest(requestId, "0x${it.r.toNoPrefixHexString()}${it.s.toNoPrefixHexString()}${encode((it.recId + 27).toByte())}") }

        val intent = Intent(Registry.mainActivity, SignMessageActivity::class.java).apply {
            putExtra(SIGN_TEXT_MESSAGE, text)
        }

        Registry.mainActivity.startActivityForResult(intent, REQ_WALLETCONNECT)
    }

    private fun signTypedData(id: Long, message: String) {
        requestId = id
        uiAction = {
            val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(message)
            val hash = typedDataHash(domainWithMessage.message, domainWithMessage.domain)
            Registry.scriptExecutor.runScript(scriptWithAuthentication().plus(SignCommand(Registry.walletConnect, hash)))
        }

        signAction = { session?.approveRequest(requestId, "0x${it.r.toNoPrefixHexString()}${it.s.toNoPrefixHexString()}${encode((it.recId + 27).toByte())}") }

        val intent = Intent(Registry.mainActivity, SignMessageActivity::class.java).apply {
            putExtra(SIGN_TEXT_MESSAGE, message)
        }

        Registry.mainActivity.startActivityForResult(intent, REQ_WALLETCONNECT)
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
                val tokenInfo = Registry.ethereumRPC.ethplorerGetTokenInfo(tx.to!!.hex)

                if (tokenInfo != null) {
                    val decimals = (tokenInfo["decimals"] as? String)?.toInt() ?: 1
                    putExtra(SIGN_TX_AMOUNT, tx.getTokenTransferValue().toTransferredAmount(decimals))
                    putExtra(SIGN_TX_CURRENCY, "${tokenInfo["name"]} (${tokenInfo["symbol"]})" )
                    putExtra(SIGN_TX_TO, tx.getTokenTransferTo().hex)
                } else {
                    putExtra(SIGN_TX_AMOUNT, tx.getTokenTransferValue().toTransferredAmount(1))
                    putExtra(SIGN_TX_CURRENCY, tx.to?.hex)
                    putExtra(SIGN_TX_TO, tx.getTokenTransferTo().hex)
                }
            } else {
                putExtra(SIGN_TX_AMOUNT, tx.value?.toTransferredAmount())
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
                fromWCUri(uri).toFullyQualifiedConfig(),
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