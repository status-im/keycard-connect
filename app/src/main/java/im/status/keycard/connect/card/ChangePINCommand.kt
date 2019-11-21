package im.status.keycard.connect.card

import im.status.keycard.connect.Registry
import java.io.IOException
import java.lang.Exception

class ChangePINCommand(private val newPIN: String) : CardCommand {
    //TODO: like for the PINCache, no strings should be used here
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            context.cmdSet.changePIN(newPIN).checkOK()
            Registry.pinCache.putPIN(context.cmdSet.applicationInfo.instanceUID, newPIN)
        }
    }
}