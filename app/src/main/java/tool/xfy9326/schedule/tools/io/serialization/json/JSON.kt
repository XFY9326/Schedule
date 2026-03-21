package tool.xfy9326.schedule.tools.io.serialization.json

import android.net.Uri
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.IOException
import okio.Sink
import okio.Source
import okio.sink
import okio.source
import tool.xfy9326.schedule.tools.io.file.AssetFile
import tool.xfy9326.schedule.tools.io.file.RawResFile
import tool.xfy9326.schedule.tools.io.okio.sink
import tool.xfy9326.schedule.tools.io.okio.source
import tool.xfy9326.schedule.tools.io.okio.useBuffer
import java.io.File
import java.io.FileNotFoundException

@Throws(SerializationException::class)
inline fun <reified T> BufferedSource.readJSON(json: Json = Json.Default): T =
    json.decodeFromString(readUtf8())

@Throws(SerializationException::class)
inline fun <reified T> BufferedSink.writeJSON(json: Json = Json.Default, data: T) {
    writeUtf8(json.encodeToString(data))
}

@Throws(FileNotFoundException::class, SerializationException::class)
inline fun <reified T> File.readJSON(json: Json = Json.Default): T =
    source().readJSON(json)

@Throws(FileNotFoundException::class, SerializationException::class)
inline fun <reified T> File.writeJSON(data: T, json: Json = Json.Default): Unit =
    sink().writeJSON(data, json)

@Throws(IOException::class, SerializationException::class)
inline fun <reified T> Uri.readJSON(json: Json = Json.Default): T =
    source().readJSON(json)

@Throws(IOException::class, SerializationException::class)
inline fun <reified T> Uri.writeJSON(data: T, json: Json = Json.Default): Unit =
    sink().writeJSON(data, json)

@Throws(IOException::class, SerializationException::class)
inline fun <reified T> AssetFile.readJSON(json: Json = Json.Default): T =
    source().readJSON(json)

@Throws(IOException::class, SerializationException::class)
inline fun <reified T> RawResFile.readJSON(json: Json = Json.Default): T =
    source().readJSON(json)

@Throws(IOException::class, SerializationException::class)
inline fun <reified T> Source.readJSON(json: Json = Json.Default): T =
    useBuffer { readJSON(json) }

@Throws(IOException::class, SerializationException::class)
inline fun <reified T> Sink.writeJSON(data: T, json: Json = Json.Default): Unit =
    useBuffer { writeJSON(json, data) }