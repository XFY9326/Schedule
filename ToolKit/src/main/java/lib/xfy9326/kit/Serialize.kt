package lib.xfy9326.kit

import java.io.*

private const val CHAR_ZERO = '0'
private const val CHAR_ONE = '1'

fun BooleanArray.serializeToString(): String {
    return buildString(size) {
        this@serializeToString.forEach { b ->
            append(if (b) CHAR_ONE else CHAR_ZERO)
        }
    }
}

fun String.deserializeToBooleanArray(): BooleanArray {
    return BooleanArray(length) { p ->
        this[p] == CHAR_ONE
    }
}

fun <T : Serializable> T.clone(): T? {
    try {
        ByteArrayOutputStream().use { byteOutput ->
            ObjectOutputStream(byteOutput).use {
                it.writeObject(this)
            }
            ObjectInputStream(ByteArrayInputStream(byteOutput.toByteArray())).use {
                return it.readObject().tryCast()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}