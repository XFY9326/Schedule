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

// O(3/2n)
fun List<Int>.minAndMax(): Pair<Int, Int>? {
    if (size == 0) return null
    if (size == 1) return this[0] to this[0]
    if (size == 2) return if (this[0] < this[1]) this[0] to this[1] else this[1] to this[0]
    var minValue: Int
    var maxValue: Int
    if (this[0] < this[1]) {
        minValue = this[0]
        maxValue = this[1]
    } else {
        minValue = this[1]
        maxValue = this[0]
    }

    var i = 2
    while (i < size) {
        if (i == size - 1) {
            if (this[i] < minValue) {
                minValue = this[i]
            } else if (this[i] > maxValue) {
                maxValue = this[i]
            }
            break
        } else {
            if (this[i] < this[i + 1]) {
                minValue = kotlin.math.min(this[i], minValue)
                maxValue = kotlin.math.max(this[i + 1], maxValue)
            } else {
                minValue = kotlin.math.min(this[i + 1], minValue)
                maxValue = kotlin.math.max(this[i], maxValue)
            }
        }
        i += 2
    }

    return minValue to maxValue
}

fun Throwable.getDeepStackTraceString() = cause?.stackTraceToString() ?: stackTraceToString()

fun <T : CharSequence> T?.nullIfBlank() = if (isNullOrBlank()) null else this