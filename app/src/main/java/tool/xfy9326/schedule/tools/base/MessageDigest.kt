@file:Suppress("unused")

package tool.xfy9326.schedule.tools.base

import java.nio.charset.Charset
import java.security.MessageDigest

fun String.md5(charset: Charset = Charsets.UTF_8): String {
    return toByteArray(charset).digest("MD5")
}

fun String.sha1(charset: Charset = Charsets.UTF_8): String {
    return toByteArray(charset).digest("SHA-1")
}

fun String.sha256(charset: Charset = Charsets.UTF_8): String {
    return toByteArray(charset).digest("SHA-256")
}

fun ByteArray.digest(algorithm: String): String {
    return MessageDigest.getInstance(algorithm).digest(this).toHex()
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
