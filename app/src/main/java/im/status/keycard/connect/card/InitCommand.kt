package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.connect.data.*
import im.status.keycard.connect.ui.InitActivity
import im.status.keycard.io.APDUException
import java.io.IOException

class InitCommand : CardCommand {
    private var initPIN: String? = null
    private var initPUK: String? = null
    private var initPairing: String? = null

    private fun promptInit(mainActivity: Activity): CommandResult {
        val intent = Intent(mainActivity, InitActivity::class.java)
        mainActivity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)
        return CommandResult.UX_ONGOING
    }

    override fun run(context: CardScriptExecutor.Context): CommandResult {
        val cmdSet = context.cmdSet ?: return CommandResult.CANCEL

        if (cmdSet.applicationInfo.isInitializedCard) {
            return CommandResult.OK
        }

        if (initPIN != null && initPUK != null && initPairing != null) {
            try {
                cmdSet.init(initPIN, initPUK, initPairing).checkOK()
                cmdSet.select().checkOK()
                cmdSet.autoPair(initPairing)
                PairingManager.putPairing(cmdSet.applicationInfo.instanceUID, cmdSet.pairing)
                return CommandResult.OK
            } catch (e: IOException) {
                return CommandResult.RETRY
            } catch (e: APDUException) {
                return CommandResult.CANCEL
            } finally {
                initPIN  = null
                initPUK = null
                initPairing = null
            }
        } else {
            return promptInit(context.mainActivity)
        }
    }

    override fun onDataReceived(data: Intent?) {
        initPIN = data?.getStringExtra(INIT_ACTIVITY_PIN)
        initPUK = data?.getStringExtra(INIT_ACTIVITY_PUK)
        initPairing = data?.getStringExtra(INIT_ACTIVITY_PAIRING)
    }
}