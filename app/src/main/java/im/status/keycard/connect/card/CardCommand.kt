package im.status.keycard.connect.card

import android.content.Intent

enum class CommandResult {
    OK, CANCEL, RETRY, UX_ONGOING
}

interface CardCommand {
    fun run(context: CardScriptExecutor.Context): CommandResult
    fun onDataReceived(data: Intent?) {}
}