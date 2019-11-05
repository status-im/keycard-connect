package im.status.keycard.connect.walletconnect

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import im.status.keycard.connect.Registry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.walletconnect.Session
import org.walletconnect.Session.Config.Companion.fromWCUri
import org.walletconnect.impls.*
import java.io.File

class SessionManager {
    private val scope = MainScope()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val okHttpClient = OkHttpClient()
    private val sessionStore = FileWCSessionStore(File(Registry.mainActivity.filesDir, "wcSessions.json").apply { createNewFile() }, moshi)
    private var session: WCSession? = null

    object SessionCB : Session.Callback {
        override fun onStatus(status: Session.Status) {
            println(status)
        }

        override fun onMethodCall(call: Session.MethodCall) {
            println(call)
        }
    }

    fun connect(uri: String) {
        scope.launch {
            session = WCSession(
                fromWCUri(uri),
                MoshiPayloadAdapter(moshi),
                sessionStore,
                OkHttpTransport.Builder(okHttpClient, moshi),
                Session.PeerMeta(name = "Keycard Connect")
            )

            session?.addCallback(SessionCB)
        }
    }
}