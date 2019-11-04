package im.status.keycard.connect.card

import java.io.IOException
import java.lang.Exception

class LoadKeyCommand : CardCommand {

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        /* TODO: this should instead prompt and ask if
         * 1. You want to generate keys on card with no backup (most secure)
         * 2. You want to generate a new key with backup phrase
         * 3. You want to import an existing key
         */

        try {
            context.cmdSet.generateKey().checkOK()
        } catch(e: IOException) {
            return CardCommand.Result.RETRY
        } catch (e: Exception) {
            return CardCommand.Result.CANCEL
        }

        return CardCommand.Result.OK
    }
}