@file:Suppress("BlockingMethodInNonBlockingContext", "unused")

package tool.xfy9326.schedule.io.utils

import android.net.Uri
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.xfy9326.android.kit.io.kt.sink
import lib.xfy9326.android.kit.io.kt.source
import lib.xfy9326.android.kit.io.kt.useBuffer
import lib.xfy9326.kit.runOnlyResultIOJob
import lib.xfy9326.kit.runSafeIOJob
import okio.*
import java.io.File

inline fun <reified T> BufferedSource.readJSON(json: Json): T = json.decodeFromString(readUtf8())

inline fun <reified T> BufferedSink.writeJSON(json: Json, data: T) = writeUtf8(json.encodeToString(data))

suspend inline fun <reified T> File.readJSON(json: Json) = readJSON<T>(source(), json)

suspend inline fun <reified T> File.writeJSON(data: T, json: Json) = writeJSON(sink(), data, json)

suspend inline fun <reified T> Uri.readJSON(json: Json) = readJSON<T>(source(), json)

suspend inline fun <reified T> Uri.writeJSON(data: T, json: Json) = writeJSON(sink(), data, json)

suspend inline fun <reified T> readJSON(source: Source?, json: Json) = runSafeIOJob {
    source?.useBuffer { readJSON<T>(json) }
}

suspend inline fun <reified T> writeJSON(sink: Sink?, data: T, json: Json) = runOnlyResultIOJob {
    sink?.useBuffer {
        writeJSON(json, data)
        true
    } ?: false
}