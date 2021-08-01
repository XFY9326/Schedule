@file:Suppress("unused", "NOTHING_TO_INLINE")

package lib.xfy9326.kit

import java.security.MessageDigest

const val NEW_LINE = "\n"

inline fun Int.isOdd(): Boolean = this % 2 != 0

inline fun Int.isEven(): Boolean = this % 2 == 0

fun String.md5(): String {
    return MessageDigest.getInstance("MD5").digest(this.toByteArray()).toHex()
}

fun ByteArray.toHex(): String {
    val builder = StringBuilder()
    for (byte in this) {
        val hex = Integer.toHexString(byte.toInt() and 0xFF)
        if (hex.length == 1) builder.append('0')
        builder.append(hex)
    }
    return builder.toString()
}

fun String.hexToByteArray(): ByteArray = ByteArray(length / 2) {
    (substring(2 * it, 2 * it + 2).toInt(16) and 0xFF).toByte()
}

inline fun <T> List<T>.forEachTwo(action: (Int, T, Int, T) -> Unit) {
    for (i1 in indices) for (i2 in (i1 + 1)..lastIndex) action(i1, this[i1], i2, this[i2])
}

fun Throwable.getDeepStackTraceString() = cause?.stackTraceToString() ?: stackTraceToString()

fun <T : CharSequence> T?.nullIfBlank() = if (isNullOrBlank()) null else this