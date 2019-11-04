package im.status.keycard.connect.card

class CheckMasterKeyCommand : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return if (context.cmdSet.applicationInfo.hasMasterKey()) CardCommand.Result.STOP else CardCommand.Result.OK
    }
}