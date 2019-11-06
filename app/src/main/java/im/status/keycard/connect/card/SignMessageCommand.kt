package im.status.keycard.connect.card

import im.status.keycard.applet.RecoverableSignature
import org.bouncycastle.jcajce.provider.digest.Keccak
import java.io.IOException
import java.lang.Exception

class SignMessageCommand(private val listener: SignListener, private val message: ByteArray) : CardCommand {
    override fun run(context: CardScriptExecutor.ScriptContext): CardCommand.Result {
        try {
            val keccak256 = Keccak.Digest256()
            val hash = keccak256.digest(byteArrayOf(0x19) + "Ethereum Signed Message:\n${message.size}".toByteArray() +  message)
            val signature = RecoverableSignature(hash, context.cmdSet.sign(hash).checkOK().data)
            listener.onResponse(signature)
        } catch(e: IOException) {
            return CardCommand.Result.RETRY
        } catch (e: Exception) {
            return CardCommand.Result.CANCEL
        }

        return CardCommand.Result.OK
    }
}