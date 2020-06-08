package im.status.keycard.connect.card

import android.net.Uri
import im.status.keycard.connect.Registry
import im.status.keycard.globalplatform.GlobalPlatformCommandSet

class ReinstallCommand(private val applet: Uri, private val installWallet: Boolean, private val installCash: Boolean, private val installNDEF: Boolean, private val cashParams: ByteArray = ByteArray(0), private val ndefParams: ByteArray = ByteArray(0)) : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return runOnCard {
            val gpCmd = GlobalPlatformCommandSet(context.channel)
            gpCmd.select().checkOK()
            gpCmd.openSecureChannel()
            gpCmd.deleteKeycardInstancesAndPackage()

            Registry.mainActivity.applicationContext.contentResolver.openInputStream(applet)?.use {
                gpCmd.loadKeycardPackage(it) {_, _ -> }
            }

            if (installWallet) { gpCmd.installKeycardApplet().checkOK() }
            if (installCash) { gpCmd.installCashApplet(cashParams).checkOK() }
            if (installNDEF) { gpCmd.installNDEFApplet(ndefParams).checkOK() }
        }
    }
}