package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.connect.Registry
import im.status.keycard.connect.ui.PINActivity
import im.status.keycard.connect.data.PIN_ACTIVITY_ATTEMPTS
import im.status.keycard.connect.data.PIN_ACTIVITY_CARD_UID
import im.status.keycard.connect.data.REQ_INTERACTIVE_SCRIPT
import im.status.keycard.io.APDUException
import im.status.keycard.io.WrongPINException
import java.io.IOException

class VerifyPINCommand : CardCommand {
    private var retries = -1

    private fun promptPIN(activity: Activity, instanceUID: ByteArray): CardCommand.Result {
        val intent = Intent(activity, PINActivity::class.java).apply {
            putExtra(PIN_ACTIVITY_ATTEMPTS, retries)
            putExtra(PIN_ACTIVITY_CARD_UID, instanceUID)
        }

        activity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)

        return CardCommand.Result.UX_ONGOING
    }

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        //TODO: handle retries == 0 with UNBLOCK PIN

        val pin = Registry.pinCache.getPIN(context.cmdSet.applicationInfo.instanceUID)

        if (pin != null) {
            try {
                context.cmdSet.verifyPIN(pin).checkAuthOK()
                retries = -1
                return CardCommand.Result.OK
            } catch (e: WrongPINException) {
                Registry.pinCache.removePIN(context.cmdSet.applicationInfo.instanceUID)
                retries = e.retryAttempts
            } catch(e: IOException) {
                return CardCommand.Result.RETRY
            } catch(e: APDUException) {
                return CardCommand.Result.CANCEL
            }
        }

        return promptPIN(context.activity, context.cmdSet.applicationInfo.instanceUID)
    }
}