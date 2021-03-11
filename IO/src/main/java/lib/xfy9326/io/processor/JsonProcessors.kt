@file:Suppress("BlockingMethodInNonBlockingContext")

package lib.xfy9326.io.processor

import android.net.Uri
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.xfy9326.io.readProcessing
import lib.xfy9326.io.target.asTarget
import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import lib.xfy9326.io.writeProcessing
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

inline fun <reified T : Any> File.jsonReader(json: Json = Json { }, charset: Charset = Charsets.UTF_8) = asTarget().jsonReader<T>(json, charset)

inline fun <reified T : Any> File.jsonWriter(json: Json = Json { }, charset: Charset = Charsets.UTF_8) = asTarget().jsonWriter<T>(json, charset)

inline fun <reified T : Any> Uri.jsonReader(json: Json = Json { }, charset: Charset = Charsets.UTF_8) = asTarget().jsonReader<T>(json, charset)

inline fun <reified T : Any> Uri.jsonWriter(json: Json = Json { }, charset: Charset = Charsets.UTF_8) = asTarget().jsonWriter<T>(json, charset)

inline fun <reified T : Any> InputTarget<out InputStream>.jsonReader(json: Json = Json { }, charset: Charset = Charsets.UTF_8) = readProcessing(T::class) {
    bufferedReader(charset).use {
        json.decodeFromString(it.readText())
    }
}

inline fun <reified T : Any> OutputTarget<out OutputStream>.jsonWriter(json: Json = Json { }, charset: Charset = Charsets.UTF_8) = writeProcessing(T::class) { data, _ ->
    bufferedWriter(charset).use {
        it.write(json.encodeToString(data))
        it.flush()
    }
}