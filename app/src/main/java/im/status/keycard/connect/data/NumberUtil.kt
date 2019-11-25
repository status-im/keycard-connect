package im.status.keycard.connect.data

import java.math.BigInteger

fun BigInteger.toTransferredAmount(decimals : Int = 18): String = this.toBigDecimal().movePointLeft(decimals).toPlainString().trimEnd('0').trimEnd('.')