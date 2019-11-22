package im.status.keycard.connect.card

class UnpairOthersCommand() : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            context.cmdSet.unpairOthers()
        }
    }
}