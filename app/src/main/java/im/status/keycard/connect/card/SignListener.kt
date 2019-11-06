package im.status.keycard.connect.card

import im.status.keycard.applet.RecoverableSignature

interface SignListener {
    fun onResponse(signature: RecoverableSignature?)
}