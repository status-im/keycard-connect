package im.status.keycard.connect.card

import im.status.keycard.applet.BIP32KeyPair
import im.status.keycard.applet.KeyPath
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.io.APDUResponse

class ExportKeyCommand(private val listener: Listener, private val path: String? = null, private val makeCurrent: Boolean = true, private val publicOnly: Boolean = true) : CardCommand {
    interface Listener {
        fun onResponse(keyPair: BIP32KeyPair)
    }

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            var response: APDUResponse? = null

            if (path != null) {
                val currentPath = KeyPath(context.cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().data)

                if (path != currentPath.toString()) {
                    response = context.cmdSet.exportKey(path, makeCurrent, publicOnly)
                }
            }

            if (response == null) {
                response = context.cmdSet.exportCurrentKey(publicOnly)
            }

            listener.onResponse(BIP32KeyPair.fromTLV(response?.checkOK()?.data))
        }
    }
}