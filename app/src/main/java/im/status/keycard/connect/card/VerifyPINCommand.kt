package im.status.keycard.connect.card

import im.status.keycard.connect.data.PINCache
import im.status.keycard.io.APDUException
import im.status.keycard.io.WrongPINException
import java.io.IOException

class VerifyPINCommand : CardCommand {
    private var retries = -1

    private fun promptPIN(): CommandResult {
        //TODO: start prompt activity
        return CommandResult.UX_ONGOING
    }

    override fun run(context: CardScriptExecutor.Context): CommandResult {
        //TODO: handle retries == 0 with UNBLOCK PIN

        val cmdSet = context.cmdSet ?: return CommandResult.CANCEL

        val pin = PINCache.getPIN(cmdSet.applicationInfo.instanceUID)

        if (pin != null) {
            try {
                cmdSet.verifyPIN(pin).checkAuthOK()
                return CommandResult.OK
            } catch (e: WrongPINException) {
                retries = e.retryAttempts
            } catch(e: IOException) {
                return CommandResult.RETRY
            } catch(e: APDUException) {
                return CommandResult.CANCEL
            }
        }

        return promptPIN()
    }
}