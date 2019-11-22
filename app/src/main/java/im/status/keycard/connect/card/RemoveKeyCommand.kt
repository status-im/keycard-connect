package im.status.keycard.connect.card

class RemoveKeyCommand() : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            context.cmdSet.removeKey().checkOK()
        }
    }
}