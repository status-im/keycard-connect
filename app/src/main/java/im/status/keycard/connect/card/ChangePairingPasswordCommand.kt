package im.status.keycard.connect.card

class ChangePairingPasswordCommand(private val pairingPassword: String) : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            context.cmdSet.changePairingPassword(pairingPassword).checkOK()
        }
    }
}