@file:Suppress("unused", "NOTHING_TO_INLINE")

package tool.xfy9326.schedule.kt

import java.io.File

const val NEW_LINE = "\n"

fun Int.isOdd(): Boolean = this % 2 != 0

fun Int.isEven(): Boolean = this % 2 == 0

fun BooleanArray.fit(): BooleanArray {
    var newSize = size
    for (i in size - 1 downTo 0) {
        if (this[i]) {
            break
        } else {
            newSize = i
        }
    }
    return when (newSize) {
        0 -> BooleanArray(0)
        size -> this
        else -> copyOfRange(0, newSize)
    }
}

fun <T> Iterable<T>?.nullableToList(): List<T> {
    return this?.toList() ?: emptyList()
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

inline fun <reified E : Enum<E>> tryEnumValueOf(name: String?): E? {
    return if (name == null) {
        null
    } else {
        try {
            enumValueOf<E>(name)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

inline fun <reified E : Enum<E>> tryEnumValueOf(names: Set<String>?): Set<E>? {
    return if (names == null) {
        null
    } else {
        HashSet<E>(names.size).apply {
            names.forEach {
                tryEnumValueOf<E>(it)?.let(::add)
            }
        }
    }
}

inline fun <E, I : Iterator<E>> I.iterate(action: I.(E) -> Unit) {
    while (hasNext()) action.invoke(this, next())
}

inline fun <T> Array<out T>.forEachTwo(action: (Int, T, Int, T) -> Unit) {
    for (i1 in indices) for (i2 in (i1 + 1)..lastIndex) action(i1, this[i1], i2, this[i2])
}

inline fun <T> List<T>.forEachTwo(action: (Int, T, Int, T) -> Unit) {
    for (i1 in indices) for (i2 in (i1 + 1)..lastIndex) action(i1, this[i1], i2, this[i2])
}

inline fun File.asParentOf(childName: String) = File(this, childName)