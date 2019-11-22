package im.status.keycard.connect.card

class ChangePUKCommand(private val newPUK: String) : CardCommand {
    //TODO: like for the PINCache, no strings should be used here
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            context.cmdSet.changePUK(newPUK).checkOK()
        }
    }
}