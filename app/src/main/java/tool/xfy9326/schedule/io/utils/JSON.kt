@file:Suppress("BlockingMethodInNonBlockingContext", "unused")

package tool.xfy9326.schedule.io.utils

import android.net.Uri
import io.github.xfy9326.atools.io.okio.sink
import io.github.xfy9326.atools.io.okio.source
import io.github.xfy9326.atools.io.okio.useBuffer
import io.github.xfy9326.atools.io.utils.runIOJob
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.*
import java.io.File

inline fun <reified T> BufferedSource.readJSON(json: Json): T = json.decodeFromString(readUtf8())

inline fun <reified T> BufferedSink.writeJSON(json: Json, data: T) = writeUtf8(json.encodeToString(data))

suspend inline fun <reified T> File.readJSON(json: Json) = readJSON<T>(source(), json)

suspend inline fun <reified T> File.writeJSON(data: T, json: Json) = writeJSON(sink(), data, json)

suspend inline fun <reified T> Uri.readJSON(json: Json) = readJSON<T>(source(), json)

suspend inline fun <reified T> Uri.writeJSON(data: T, json: Json) = writeJSON(sink(), data, json)

suspend inline fun <reified T> readJSON(source: Source?, json: Json): T? = runIOJob {
    source?.useBuffer { readJSON<T>(json) }
}.getOrNull()

suspend inline fun <reified T> writeJSON(sink: Sink?, data: T, json: Json): Boolean = runIOJob {
    sink?.useBuffer {
        writeJSON(json, data)
        true
    } ?: false
}.getOrDefault(false)