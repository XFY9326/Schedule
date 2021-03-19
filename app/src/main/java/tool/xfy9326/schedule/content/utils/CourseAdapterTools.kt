@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.kt.tryCast
import java.io.*

fun BooleanArray.arrangeWeekNum(): BooleanArray {
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

fun BooleanArray.hasCourse(num: Int): Boolean {
    val index = num - 1
    return if (index in indices) {
        this[index]
    } else {
        false
    }
}

fun List<Course>.arrangeWeekNum() {
    this.forEach {
        it.times.forEach { time ->
            time.weekNum = time.weekNum.arrangeWeekNum()
        }
    }
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