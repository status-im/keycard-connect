package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.io.CardChannel
import im.status.keycard.io.CardListener
import java.util.*

const val REQ_INTERACTIVE_SCRIPT = 0x01

class CardScriptExecutor(activity: Activity) : CardListener {
    class Context(val mainActivity: Activity) {
        var cardChannel: CardChannel? = null
        var cmdSet: KeycardCommandSet? = null
    }

    enum class State {
        READY, UX_ONGOING, RUNNING
    }

    private var state = State.READY
    private var executionContext = Context(activity)
    private var script: List<CardCommand>? = null
    private var waitingCmd: CardCommand? = null

    override fun onConnected(channel: CardChannel) {
        executionContext.cardChannel = channel
        executionContext.cmdSet = KeycardCommandSet(executionContext.cardChannel)

        if (state == State.READY) {
            state = State.RUNNING
        } else if (state == State.UX_ONGOING) {
            return
        }

        //TODO: replace with default script
        val runningScript = script ?: LinkedList()

        for (cmd in runningScript) {
            when (cmd.run(executionContext)) {
                CommandResult.OK -> {}
                CommandResult.CANCEL -> { state = State.READY; return }
                CommandResult.UX_ONGOING -> { waitingCmd = cmd; return }
                CommandResult.RETRY -> { return }
            }
        }
    }

    override fun onDisconnected() {
        executionContext.cardChannel = null
        executionContext.cmdSet = null
    }

    fun onUserInteractionReturned(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            waitingCmd?.onDataReceived(data)
            state = State.RUNNING
        } else {
            state = State.READY
        }
    }

    fun setScript(script: List<CardCommand>): Boolean {
        if (state == State.READY) {
            this.script = script
            return true
        }

        return false
    }
}