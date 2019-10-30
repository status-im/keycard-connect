package im.status.keycard.connect.data

import im.status.keycard.applet.Pairing

object PairingManager {
    //TODO: persistency

    private val pairings: MutableMap<ByteArrayKey, Pairing> = HashMap()

    fun getPairing(instanceUID: ByteArray): Pairing? {
        return pairings[ByteArrayKey(instanceUID)]
    }

    fun putPairing(instanceUID: ByteArray, pairing: Pairing) {
        pairings[ByteArrayKey(instanceUID)] = pairing
    }

    fun removePairing(instanceUID: ByteArray) {
        pairings.remove(ByteArrayKey(instanceUID))
    }
}