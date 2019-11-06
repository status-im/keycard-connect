package im.status.keycard.connect.card

fun scriptWithSecureChannel(): List<CardCommand> = listOf(SelectCommand(), InitCommand(), OpenSecureChannelCommand())
fun scriptWithAuthentication(): List<CardCommand> = scriptWithSecureChannel().plus(VerifyPINCommand())
fun cardCheckupScript(): List<CardCommand> = scriptWithSecureChannel().plus(CheckMasterKeyCommand()).plus(VerifyPINCommand()).plus(LoadKeyCommand())

fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
