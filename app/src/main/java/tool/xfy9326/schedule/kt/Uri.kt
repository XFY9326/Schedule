@file:Suppress("unused")

package tool.xfy9326.schedule.kt

import android.net.Uri
import tool.xfy9326.schedule.io.GlobalIO
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

fun Uri.asInputStream() = GlobalIO.contentResolver.openInputStream(this)

fun Uri.asOutputStream() = GlobalIO.contentResolver.openOutputStream(this)

fun Uri.writeText(text: String, charset: Charset = StandardCharsets.UTF_8) = writeBytes(text.toByteArray(charset))

fun Uri.writeBytes(byteArray: ByteArray) = asOutputStream()?.use { it.write(byteArray) } != null

fun Uri.readText(charset: Charset = StandardCharsets.UTF_8) = asInputStream()?.reader(charset)?.use { it.readText() }

fun Uri.readBytes() = asInputStream()?.use { it.readBytes() }