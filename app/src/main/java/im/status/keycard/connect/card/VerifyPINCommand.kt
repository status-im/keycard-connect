package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.connect.Registry
import im.status.keycard.connect.data.*
import im.status.keycard.connect.ui.PINActivity
import im.status.keycard.connect.ui.PUKActivity
import im.status.keycard.io.APDUException
import im.status.keycard.io.WrongPINException
import java.io.IOException

class VerifyPINCommand(private var retries: Int = -1) : CardCommand {
    private var pukRetries = -1

    private fun promptPIN(activity: Activity, instanceUID: ByteArray): CardCommand.Result {
        val intent = Intent(activity, PINActivity::class.java).apply {
            putExtra(PIN_ACTIVITY_ATTEMPTS, retries)
            putExtra(PIN_ACTIVITY_CARD_UID, instanceUID)
        }

        activity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)

        return CardCommand.Result.UX_ONGOING
    }

    private fun promptPUK(activity: Activity): CardCommand.Result {
        val intent = Intent(activity, PUKActivity::class.java).apply {
            putExtra(PUK_ACTIVITY_ATTEMPTS, pukRetries)
        }

        activity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)

        return CardCommand.Result.UX_ONGOING
    }

    private fun unblockPIN(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        val pukAndPIN: Pair<String, String>? = Registry.pinCache.pukAndPIN

        if (pukAndPIN != null) {
            try {
                context.cmdSet.unblockPIN(pukAndPIN.first, pukAndPIN.second).checkAuthOK()
                Registry.pinCache.putPIN(context.cmdSet.applicationInfo.instanceUID, pukAndPIN.second)
                retries = -1
                pukRetries = -1
                return CardCommand.Result.OK
            } catch (e: WrongPINException) {
                pukRetries = e.retryAttempts
            } catch(e: IOException) {
                return CardCommand.Result.RETRY
            } catch(e: APDUException) {
                return CardCommand.Result.CANCEL
            } finally {
                Registry.pinCache.pukAndPIN = null
            }
        }

        return promptPUK(context.activity)
    }

    private fun verifyPIN(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
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

        return if (retries == 0) promptPUK(context.activity) else promptPIN(context.activity, context.cmdSet.applicationInfo.instanceUID)
    }

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return if (retries == 0) unblockPIN(context) else verifyPIN(context)
    }
}