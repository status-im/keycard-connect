package im.status.keycard.connect.card

class SelectCommand : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        //TODO: handle not-installed-applet/not-a-keycard
        return runOnCard {
            context.cmdSet.select().checkOK()
        }
    }
}