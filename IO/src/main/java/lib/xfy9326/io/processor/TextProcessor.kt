@file:Suppress("BlockingMethodInNonBlockingContext")

package lib.xfy9326.io.processor

import android.net.Uri
import lib.xfy9326.io.readProcessing
import lib.xfy9326.io.target.asTarget
import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import lib.xfy9326.io.writeProcessing
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

fun File.textReader(charset: Charset = Charsets.UTF_8) = asTarget().textReader(charset)

fun File.textWriter(charset: Charset = Charsets.UTF_8) = asTarget().textWriter(charset)

fun Uri.textReader(charset: Charset = Charsets.UTF_8) = asTarget().textReader(charset)

fun Uri.textWriter(charset: Charset = Charsets.UTF_8) = asTarget().textWriter(charset)

fun InputTarget<out InputStream>.textReader(charset: Charset = Charsets.UTF_8) = readProcessing(String::class) {
    bufferedReader(charset).use {
        it.readText()
    }
}

fun OutputTarget<out OutputStream>.textWriter(charset: Charset = Charsets.UTF_8) = writeProcessing(String::class) { data, _ ->
    bufferedWriter(charset).use {
        it.write(data)
        it.flush()
    }
}
