package im.status.keycard.connect.card

import android.app.Activity
import android.content.Intent
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.applet.Mnemonic
import im.status.keycard.connect.data.*
import im.status.keycard.connect.ui.LoadKeyActivity
import java.io.IOException
import java.lang.Exception

class LoadKeyCommand(private var loadType: Int = LOAD_NONE, private var mnemonic: String? = null) : CardCommand {
    private fun promptKey(activity: Activity) : CardCommand.Result {
        val intent = Intent(activity, LoadKeyActivity::class.java)
        activity.startActivityForResult(intent, REQ_INTERACTIVE_SCRIPT)
        return CardCommand.Result.UX_ONGOING
    }

    private fun showMnemonic(activity: Activity, m: Mnemonic) {
        //TODO: implement show mnemonic screen
        println(m.toMnemonicPhrase())
    }

    private fun generateKey(cmdSet: KeycardCommandSet): CardCommand.Result {
        return runOnCard {
            cmdSet.generateKey().checkOK()
        }
    }

    private fun generateBIP39(activity: Activity, cmdSet: KeycardCommandSet): CardCommand.Result {
        return runOnCard {
            val m = Mnemonic(cmdSet.generateMnemonic(KeycardCommandSet.GENERATE_MNEMONIC_12_WORDS).checkOK().data)
            m.fetchBIP39EnglishWordlist()
            cmdSet.loadKey(m.toBIP32KeyPair()).checkOK()
            showMnemonic(activity, m)
        }
    }

    private fun importBIP39(cmdSet: KeycardCommandSet): CardCommand.Result {
        return runOnCard {
            cmdSet.loadKey(Mnemonic.toBinarySeed(mnemonic, "")).checkOK()
        }
    }

    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        return when (loadType) {
            LOAD_NONE -> promptKey(context.activity)
            LOAD_GENERATE -> generateKey(context.cmdSet)
            LOAD_GENERATE_BIP39 -> generateBIP39(context.activity, context.cmdSet)
            LOAD_IMPORT_BIP39 -> importBIP39(context.cmdSet)
            else -> CardCommand.Result.CANCEL
        }
    }

    override fun onDataReceived(data: Intent?) {
        loadType = data?.getIntExtra(LOAD_TYPE, LOAD_NONE) ?: LOAD_NONE

        if (loadType == LOAD_IMPORT_BIP39) {
            mnemonic = data?.getStringExtra(LOAD_MNEMONIC)
        }
    }
}