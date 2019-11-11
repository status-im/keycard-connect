package im.status.keycard.connect.net

import org.kethereum.extensions.maybeHexToBigInteger
import org.kethereum.rpc.HttpEthereumRPC
import org.kethereum.rpc.model.StringResultResponse
import java.io.IOException
import java.math.BigInteger

class EthereumRPC(endpointURL: String) {
    private var endpoint = HttpEthereumRPC(endpointURL)

    fun changeEndpoint(endpointURL: String) {
        endpoint = HttpEthereumRPC(endpointURL)
    }

    private inline fun <T> valueOrThrow(res: StringResultResponse?, body: (String) -> T) : T {
        if (res != null && res.error == null) {
            return body(res.result)
        } else {
            throw IOException("communication error")
        }
    }

    fun ethGetTransactionCount(address: String): BigInteger {
        return valueOrThrow(endpoint.getTransactionCount(address)) { it.maybeHexToBigInteger() }
    }

    fun ethGasPrice(): BigInteger {
        return valueOrThrow(endpoint.gasPrice()) { it.maybeHexToBigInteger() }
    }

    fun ethSendRawTransaction(rawTx: String): String {
        return valueOrThrow(endpoint.sendRawTransaction(rawTx)) { it }
    }
}