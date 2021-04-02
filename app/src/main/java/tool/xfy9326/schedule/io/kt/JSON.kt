package tool.xfy9326.schedule.io.kt

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

inline fun <reified T> BufferedSource.readJSON(json: Json): T = json.decodeFromString(readUtf8())

inline fun <reified T> BufferedSink.writeJSON(json: Json, data: T) = writeUtf8(json.encodeToString(data))