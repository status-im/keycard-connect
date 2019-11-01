package im.status.keycard.connect.card

import java.io.IOException
import java.lang.Exception

class SelectCommand : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        //TODO: handle not-installed-applet/not-a-keycard
        try {
            context.cmdSet.select().checkOK()
        } catch(e: IOException) {
            return CardCommand.Result.RETRY
        } catch (e: Exception) {
            return CardCommand.Result.CANCEL
        }

        return CardCommand.Result.OK
    }
}