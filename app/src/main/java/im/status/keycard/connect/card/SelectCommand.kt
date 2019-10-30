package im.status.keycard.connect.card

import java.io.IOException
import java.lang.Exception

class SelectCommand : CardCommand {
    override fun run(context: CardScriptExecutor.Context): CommandResult {
        //TODO: handle not-installed-applet/not-a-keycard
        try {
            context.cmdSet!!.select().checkOK()
        } catch(e: IOException) {
            return CommandResult.RETRY
        } catch (e: Exception) {
            return CommandResult.CANCEL
        }

        return CommandResult.OK
    }
}