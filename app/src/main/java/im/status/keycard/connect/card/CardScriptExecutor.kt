package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.io.CardChannel
import im.status.keycard.io.CardListener

class CardScriptExecutor(private val activity: Activity) : CardListener {
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
            state = State.RUNNING
        } else if (state == State.UX_ONGOING) {
            return
        }

        val runningScript = script ?: defaultScript ?: return

        script@for (cmd in runningScript) {
            when (cmd.run(executionContext)) {
                CardCommand.Result.OK -> {}
                CardCommand.Result.CANCEL -> { break@script}
                CardCommand.Result.UX_ONGOING -> { waitingCmd = cmd; return }
                CardCommand.Result.RETRY -> { return }
            }
        }

        script = null
        state = State.READY
    }

    override fun onDisconnected() {

    }

    fun onUserInteractionReturned(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            waitingCmd?.onDataReceived(data)
            state = State.RUNNING
        } else {
            script = null
            state = State.READY
        }
    }

    fun runScript(script: List<CardCommand>): Boolean {
        if (state == State.READY) {
            this.script = script
            return true
        }

        return false
    }

    fun cancelScript() {
        script = null
        state = State.READY
    }
}