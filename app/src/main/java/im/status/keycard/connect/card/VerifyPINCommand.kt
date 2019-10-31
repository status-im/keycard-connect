package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.connect.PINActivity
import im.status.keycard.connect.PIN_ACTIVITY_ATTEMPTS
import im.status.keycard.connect.PIN_ACTIVITY_CARD_UID
import im.status.keycard.connect.data.PINCache
import im.status.keycard.io.APDUException
import im.status.keycard.io.WrongPINException
import java.io.IOException

class VerifyPINCommand : CardCommand {
    private var retries = -1

    private fun promptPIN(mainActivity: Activity, instanceUID: ByteArray): CommandResult {
        val intent = Intent(mainActivity, PINActivity::class.java).apply {
            putExtra(PIN_ACTIVITY_ATTEMPTS, retries)
            putExtra(PIN_ACTIVITY_CARD_UID, instanceUID)
        }


        mainActivity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)
        return CommandResult.UX_ONGOING
    }

    override fun run(context: CardScriptExecutor.Context): CommandResult {
        //TODO: handle retries == 0 with UNBLOCK PIN

        val cmdSet = context.cmdSet ?: return CommandResult.CANCEL

        val pin = PINCache.getPIN(cmdSet.applicationInfo.instanceUID)

        if (pin != null) {
            try {
                cmdSet.verifyPIN(pin).checkAuthOK()
                retries = -1;
                return CommandResult.OK
            } catch (e: WrongPINException) {
                PINCache.removePIN(cmdSet.applicationInfo.instanceUID)
                retries = e.retryAttempts
            } catch(e: IOException) {
                return CommandResult.RETRY
            } catch(e: APDUException) {
                return CommandResult.CANCEL
            }
        }

        return promptPIN(context.mainActivity, cmdSet.applicationInfo.instanceUID)
    }
}