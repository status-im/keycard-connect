package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.connect.*
import im.status.keycard.connect.data.PairingManager
import java.io.IOException

class OpenSecureChannelCommand : CardCommand {
    private var pairingPassword: String? = null

    private fun openSecureChannel(cmdSet: KeycardCommandSet): CommandResult {
        try {
            cmdSet.autoOpenSecureChannel()
        } catch (e: IOException) {
            //TODO: must distinguish real IOException from card exception (to fix in SDK)
            return CommandResult.CANCEL
        }

        return CommandResult.OK
    }

    private fun pair(mainActivity: Activity, cmdSet: KeycardCommandSet): CommandResult {
        if (pairingPassword != null) {
            try {
                //TODO: must distinguish real IOException from card exception (to fix in SDK)
                cmdSet.autoPair(pairingPassword)
                PairingManager.putPairing(cmdSet.applicationInfo.instanceUID, cmdSet.pairing)
                cmdSet.autoOpenSecureChannel()
                return CommandResult.OK
            } finally {
                pairingPassword = null
            }
        }

        return promptPairingPassword(mainActivity)
    }

    private fun promptPairingPassword(mainActivity: Activity): CommandResult {
        val intent = Intent(mainActivity, PairingActivity::class.java)
        mainActivity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)

        return CommandResult.UX_ONGOING
    }

    override fun run(context: CardScriptExecutor.Context): CommandResult {
        val cmdSet = context.cmdSet ?: return CommandResult.CANCEL

        val pairing = PairingManager.getPairing(cmdSet.applicationInfo.instanceUID)

        if (pairing != null) {
            cmdSet.pairing = pairing
            if (openSecureChannel(cmdSet) == CommandResult.CANCEL) {
                PairingManager.removePairing(cmdSet.applicationInfo.instanceUID)
            } else {
                return CommandResult.OK
            }
        }

        return pair(context.mainActivity, cmdSet)
    }

    override fun onDataReceived(data: Intent?) {
        pairingPassword = data?.getStringExtra(PAIRING_ACTIVITY_PASSWORD)
    }
}