package im.status.keycard.connect.card

import java.io.IOException
import java.lang.Exception

class SelectCommand : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        //TODO: handle not-installed-applet/not-a-keycard
        return runOnCard {
            context.cmdSet.select().checkOK()
        }
    }
}