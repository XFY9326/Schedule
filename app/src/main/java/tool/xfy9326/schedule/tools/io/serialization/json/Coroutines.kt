@file:Suppress("unused")

package tool.xfy9326.schedule.tools.io.serialization.json

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.tools.io.file.AssetFile
import tool.xfy9326.schedule.tools.io.file.RawResFile
import java.io.File

suspend inline fun <reified T> File.readJSONAsync(json: Json = Json.Default): Result<T> =
    withContext(Dispatchers.IO) {
        runCatching {
            readJSON(json)
        }
    }

suspend inline fun <reified T> File.writeJSONAsync(data: T, json: Json = Json.Default): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            writeJSON(data, json)
        }
    }

suspend inline fun <reified T> Uri.readJSONAsync(json: Json = Json.Default): Result<T> =
    withContext(Dispatchers.IO) {
        runCatching {
            readJSON(json)
        }
    }

suspend inline fun <reified T> Uri.writeJSONAsync(data: T, json: Json = Json.Default): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            writeJSON(data, json)
        }
    }

suspend inline fun <reified T> AssetFile.readJSONAsync(json: Json = Json.Default): Result<T> =
    withContext(Dispatchers.IO) {
        runCatching {
            readJSON(json)
        }
    }

suspend inline fun <reified T> RawResFile.readJSONAsync(json: Json = Json.Default): Result<T> =
    withContext(Dispatchers.IO) {
        runCatching {
            readJSON(json)
        }
    }
