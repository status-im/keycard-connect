package im.status.keycard.connect.card

import android.content.Intent

interface CardCommand {
    enum class Result {
        OK, CANCEL, RETRY, UX_ONGOING
    }

    fun run(context: CardScriptExecutor.ScriptContext): Result
    fun onDataReceived(data: Intent?) {}
}