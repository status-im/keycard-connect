package im.status.keycard.connect.card

import im.status.keycard.connect.Registry

class UnpairCommand() : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            context.cmdSet.autoUnpair()
            Registry.pairingManager.removePairing(context.cmdSet.applicationInfo.instanceUID)
        }
    }
}