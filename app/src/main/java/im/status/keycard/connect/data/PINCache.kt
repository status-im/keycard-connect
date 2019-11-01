package im.status.keycard.connect.data

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class PINCache {
    //TODO: don't use Strings, the memory should be cleared before release. For this the entire
    // chain from the EditText to the SDK should be controlled and never generate a String object.
    // This will require extensions to the SDK.
    private val pins: MutableMap<ByteArrayKey, String> = HashMap()
    private val timestamps: MutableMap<Long, ByteArrayKey> = HashMap()

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    init {
        scheduler.scheduleAtFixedRate(this::cleanCache, 1, 1, TimeUnit.MINUTES)
    }

    private fun cleanCache() {
        val now: Long = System.currentTimeMillis()
        timestamps.filterKeys { (now - it) < CACHE_VALIDITY }
        pins.filterKeys { timestamps.containsValue(it) }
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