package im.status.keycard.connect.data

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class PINCache {
    //TODO: don't use Strings, the memory should be cleared before release. For this the entire
    // chain from the EditText to the SDK should be controlled and never generate a String object.
    // This will require extensions to the SDK.
    private val pins: MutableMap<ByteArrayKey, String> = HashMap()
    private val timestamps: MutableMap<Long, ByteArrayKey> = HashMap()

    //This is needed to avoid passing PUK and new PIN with Intents, which could make unwanted copies
    var pukAndPIN: Pair<String, String>? = null
    private var latestPUKandPINHashCode: Int = 0

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    init {
        scheduler.scheduleAtFixedRate(this::cleanCache, 1, 1, TimeUnit.MINUTES)
    }

    private fun cleanCache() {
        val now: Long = System.currentTimeMillis()
        timestamps.filterKeys { (now - it) < CACHE_VALIDITY }
        pins.filterKeys { timestamps.containsValue(it) }

        //whatever happens, lets not leave PUK in cache more than 2 cache cleaning cycles
        if (pukAndPIN != null) {
            if (latestPUKandPINHashCode == pukAndPIN.hashCode()) {
                pukAndPIN = null
            } else {
                latestPUKandPINHashCode = pukAndPIN.hashCode()
            }
        }
    }

    fun getPIN(instanceUID: ByteArray): String? {
        return pins[ByteArrayKey(instanceUID)]
    }

    fun putPIN(instanceUID: ByteArray, pin: String) {
        val key = ByteArrayKey(instanceUID)
        timestamps[System.currentTimeMillis()] = key
        pins[key] = pin
    }

    fun removePIN(instanceUID: ByteArray) {
        val key = ByteArrayKey(instanceUID)

        timestamps.filterValues { it != key }
        pins.remove(key)
    }
}