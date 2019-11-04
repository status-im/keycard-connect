package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.io.CardChannel
import im.status.keycard.io.CardListener

class CardScriptExecutor(private val activity: Activity, private val listener: ScriptListener) : CardListener {
    class ScriptContext(val activity: Activity, val cmdSet: KeycardCommandSet)

    enum class State {
        READY, UX_ONGOING, RUNNING
    }

    var defaultScript: List<CardCommand>? = null

    private var state = State.READY
    private var script: List<CardCommand>? = null
    private var waitingCmd: CardCommand? = null

    override fun onConnected(channel: CardChannel) {
        val executionContext = ScriptContext(activity, KeycardCommandSet(channel))

        if (state == State.READY) {
            startScript()
        } else if (state == State.UX_ONGOING) {
            return
        }

        val runningScript = script ?: defaultScript ?: return

        var success = true

        script@for (cmd in runningScript) {
            when (cmd.run(executionContext)) {
                CardCommand.Result.OK -> {}
                CardCommand.Result.STOP -> { break@script }
                CardCommand.Result.CANCEL -> { success = false; break@script }
                CardCommand.Result.UX_ONGOING -> { waitingCmd = cmd; return }
                CardCommand.Result.RETRY -> { return }
            }
        }

        finishScript(if (success) CardCommand.Result.OK else CardCommand.Result.CANCEL)
    }

    override fun onDisconnected() {

    }

    fun onUserInteractionReturned(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            waitingCmd?.onDataReceived(data)
            state = State.RUNNING
        } else {
            cancelScript()
        }
    }

    fun runScript(script: List<CardCommand>): Boolean {
        if (state == State.READY) {
            this.script = script
            startScript()
            return true
        }

        return false
    }

    fun cancelScript() = finishScript(CardCommand.Result.CANCEL)

    private fun finishScript(result: CardCommand.Result) {
        script = null
        state = State.READY
        listener.onScriptFinished(result)
    }

    private fun startScript() {
        state = State.RUNNING
        listener.onScriptStarted()
    }
}