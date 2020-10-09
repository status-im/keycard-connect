package im.status.keycard.connect.net

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kethereum.rpc.HttpEthereumRPC
import java.lang.Exception
import java.lang.reflect.Type
import java.math.BigInteger


class EthereumRPC(endpointURL: String) {
    private var endpoint = HttpEthereumRPC(endpointURL)
    private val ethplorerClient = OkHttpClient().newBuilder().build()
    private val ethplorerJSONAdapter: JsonAdapter<Map<String, Any>>

    init {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        ethplorerJSONAdapter = moshi.adapter<Map<String, Any>>(type)
    }

    fun changeEndpoint(endpointURL: String) {
        endpoint = HttpEthereumRPC(endpointURL)
    }

    fun ethGetTransactionCount(address: String): BigInteger {
        return endpoint.getTransactionCount(address)!!
    }

    fun ethGasPrice(): BigInteger {
        return endpoint.gasPrice()!!
    }

    fun ethSendRawTransaction(rawTx: String): String {
        return endpoint.sendRawTransaction(rawTx)!!
    }

    fun ethplorerGetTokenInfo(address: String): Map<String, Any>? {
        //TODO: add a personalized API key
        try {
            val request = Request.Builder().url("https://api.ethplorer.io/getTokenInfo/${address}?apiKey=freekey").build()
            val response = ethplorerClient.newCall(request).execute().body.use { it?.string() } ?: return null
            return ethplorerJSONAdapter.fromJson(response)
        } catch(e: Exception) {
            return null
        }
    }
}