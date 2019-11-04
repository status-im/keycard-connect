package im.status.keycard.connect.card

interface ScriptListener {
    fun onScriptStarted()
    fun onScriptFinished(result: CardCommand.Result)
}