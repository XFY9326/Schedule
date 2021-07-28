@file:Suppress("unused")

package tool.xfy9326.schedule.content.utils

import androidx.annotation.IntRange
import org.jsoup.nodes.Element
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.kt.tryCast
import java.io.*
import java.security.MessageDigest

private const val CHAR_ZERO = '0'
private const val CHAR_ONE = '1'

/**
 * 重新整理周数数组
 * 减少不必要的尾部信息
 * @return 周数数组
 */
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

fun List<Course>.arrangeCourseWeekNum() {
    this.forEach {
        it.times.forEach { time ->
            time.weekNum = time.weekNum.arrangeWeekNum()
        }
    }
}

fun Course.arrangeWeekNum() {
    this.times.forEach {
        it.weekNum = it.weekNum.arrangeWeekNum()
    }
}

/**
 * 是否有课程
 *
 * @param num 节次（从1开始）
 * @return 是否有课程
 */
fun BooleanArray.hasCourse(@IntRange(from = 1) num: Int): Boolean {
    val index = num - 1
    return if (index in indices) {
        this[index]
    } else {
        false
    }
}

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

/**
 * 整形Collection转BooleanArray
 * 1 -> Index 0
 * 2 -> Index 1
 *
 * @return BooleanArray
 */
fun Collection<Int>.toBooleanArray(): BooleanArray {
    val max = maxOrNull()
    return if (max == null) {
        BooleanArray(0)
    } else {
        BooleanArray(max).also {
            for (i in this) {
                it[i - 1] = true
            }
        }
    }
}

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

fun Element.selectSingle(cssQuery: String) = selectFirst(cssQuery) ?: throw NoSuchElementException("No element found by css selector! $cssQuery")