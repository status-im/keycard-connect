package im.status.keycard.connect.data

fun isValidPIN(pin: String) : Boolean = pin.length == 6 && pin.all { it.isDigit() }
fun isValidPUK(puk: String) : Boolean = puk.length == 12 && puk.all { it.isDigit() }
