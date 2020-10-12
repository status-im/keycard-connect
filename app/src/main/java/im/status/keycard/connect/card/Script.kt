package im.status.keycard.connect.card

import java.io.IOException

fun scriptWithSecureChannel(): List<CardCommand> = listOf(SelectCommand(), InitCommand(), OpenSecureChannelCommand())
fun scriptWithAuthentication(): List<CardCommand> = scriptWithSecureChannel().plus(VerifyPINCommand())
fun cardCheckupScript(): List<CardCommand> = scriptWithSecureChannel().plus(CheckMasterKeyCommand()).plus(VerifyPINCommand()).plus(LoadKeyCommand())

fun runOnCard(body: () -> Unit) : CardCommand.Result {
    try {
        body()
    } catch(e: IOException) {
        return CardCommand.Result.RETRY
    } catch (e: Exception) {
        return CardCommand.Result.CANCEL
    }

    return CardCommand.Result.OK
}
