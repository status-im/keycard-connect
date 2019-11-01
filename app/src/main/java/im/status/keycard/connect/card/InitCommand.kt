package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.connect.Registry
import im.status.keycard.connect.data.*
import im.status.keycard.connect.ui.InitActivity
import im.status.keycard.io.APDUException
import java.io.IOException

class InitCommand : CardCommand {
    private var initPIN: String? = null
    private var initPUK: String? = null
    private var initPairing: String? = null

    private fun promptInit(activity: Activity): CardCommand.Result {
        val intent = Intent(activity, InitActivity::class.java)
        activity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)
        return CardCommand.Result.UX_ONGOING
    }

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        if (context.cmdSet.applicationInfo.isInitializedCard) {
            return CardCommand.Result.OK
        }

        if (initPIN != null && initPUK != null && initPairing != null) {
            try {
                context.cmdSet.init(initPIN, initPUK, initPairing).checkOK()
                context.cmdSet.select().checkOK()
                context.cmdSet.autoPair(initPairing)
                Registry.pairingManager.putPairing(context.cmdSet.applicationInfo.instanceUID, context.cmdSet.pairing)
                return CardCommand.Result.OK
            } catch (e: IOException) {
                return CardCommand.Result.RETRY
            } catch (e: APDUException) {
                return CardCommand.Result.CANCEL
            } finally {
                initPIN  = null
                initPUK = null
                initPairing = null
            }
        } else {
            return promptInit(context.activity)
        }
    }

    override fun onDataReceived(data: Intent?) {
        initPIN = data?.getStringExtra(INIT_ACTIVITY_PIN)
        initPUK = data?.getStringExtra(INIT_ACTIVITY_PUK)
        initPairing = data?.getStringExtra(INIT_ACTIVITY_PAIRING)
    }
}