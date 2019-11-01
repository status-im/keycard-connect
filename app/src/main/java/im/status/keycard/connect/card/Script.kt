package im.status.keycard.connect.card

fun scriptWithSecureChannel(): List<CardCommand> = listOf(SelectCommand(), InitCommand(), OpenSecureChannelCommand())
fun scriptWithAuthentication(): List<CardCommand> = scriptWithSecureChannel().plus(VerifyPINCommand())
fun cardCheckupScript(): List<CardCommand> = scriptWithAuthentication()

