package im.status.keycard.connect.card

import im.status.keycard.applet.KeyPath
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.applet.RecoverableSignature
import im.status.keycard.io.APDUResponse
import java.io.IOException
import java.lang.Exception

class SignCommand(private val listener: Listener, private val hash: ByteArray, private val path: String? = null, private val makeCurrent: Boolean = true, private val pinless: Boolean = false) : CardCommand {
    interface Listener {
        fun onResponse(signature: RecoverableSignature)
    }

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            var response: APDUResponse? = null

            if (path != null) {
                val currentPath = KeyPath(context.cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().data)

                if (path != currentPath.toString()) {
                    response = context.cmdSet.signWithPath(hash, path, makeCurrent)
                }
            }

            if (response == null) {
                response = if (pinless) {
                    context.cmdSet.signPinless(hash)
                } else {
                    context.cmdSet.sign(hash)
                }
            }

            val signature = RecoverableSignature(hash, response?.checkOK()?.data)
            listener.onResponse(signature)
        }
    }
}