package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.connect.Registry
import im.status.keycard.connect.data.PAIRING_ACTIVITY_PASSWORD
import im.status.keycard.connect.data.REQ_INTERACTIVE_SCRIPT
import im.status.keycard.connect.ui.PairingActivity
import java.io.IOException

class OpenSecureChannelCommand : CardCommand {
    private var pairingPassword: String? = null

    private fun openSecureChannel(cmdSet: KeycardCommandSet): CardCommand.Result {
        try {
            cmdSet.autoOpenSecureChannel()
        } catch (e: IOException) {
            //TODO: must distinguish real IOException from card exception (to fix in SDK)
            return CardCommand.Result.CANCEL
        }

        return CardCommand.Result.OK
    }

    private fun pair(activity: Activity, cmdSet: KeycardCommandSet): CardCommand.Result {
        if (pairingPassword != null) {
            try {
                //TODO: must distinguish real IOException from card exception (to fix in SDK)
                cmdSet.autoPair(pairingPassword)
                Registry.pairingManager.putPairing(cmdSet.applicationInfo.instanceUID, cmdSet.pairing)
                cmdSet.autoOpenSecureChannel()
                return CardCommand.Result.OK
            } catch(e: IOException) {
                e.printStackTrace()
            } finally {
                pairingPassword = null
            }
        }

        return promptPairingPassword(activity)
    }

    private fun promptPairingPassword(mainActivity: Activity): CardCommand.Result {
        val intent = Intent(mainActivity, PairingActivity::class.java)
        mainActivity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)

        return CardCommand.Result.UX_ONGOING
    }

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        val pairing = Registry.pairingManager.getPairing(context.cmdSet.applicationInfo.instanceUID)

        if (pairing != null) {
            context.cmdSet.pairing = pairing
            if (openSecureChannel(context.cmdSet) == CardCommand.Result.CANCEL) {
                Registry.pairingManager.removePairing(context.cmdSet.applicationInfo.instanceUID)
            } else {
                return CardCommand.Result.OK
            }
        }

        return pair(context.activity, context.cmdSet)
    }

    override fun onDataReceived(data: Intent?) {
        pairingPassword = data?.getStringExtra(PAIRING_ACTIVITY_PASSWORD)
    }
}