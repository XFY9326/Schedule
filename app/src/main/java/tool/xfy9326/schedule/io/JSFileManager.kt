@file:Suppress("BlockingMethodInNonBlockingContext")

package tool.xfy9326.schedule.io

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.xfy9326.kit.*
import okio.Source
import okio.sink
import okio.source
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.make
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.io.JSFileManager.SaveType.*
import tool.xfy9326.schedule.io.kt.readJSON
import tool.xfy9326.schedule.io.kt.useBuffer
import tool.xfy9326.schedule.io.kt.writeJSON
import java.io.InputStream

/**
 * Dir sample:
 * |----- config.json
 * |----- src
 *        |----- ScheduleProvider.js
 *        |----- ScheduleParser.js
 * |----- lib
 *        |-----[Url MD5].js
 *
 * File download:
 * XXX.js.download -> XXX.js
 */
object JSFileManager {
    private const val FILE_NAME_JS_CONFIG = "config.json"
    private const val FILE_NAME_JS_PROVIDER = "provider.js"
    private const val FILE_NAME_JS_PARSER = "parser.js"

    private const val DIR_SRC = "src"
    private const val DIR_LIB = "lib"

    private const val EXTENSION_JS = "js"
    private const val EXTENSION_DOWNLOAD = "download"

    enum class SaveType {
        PROVIDER,
        PARSER,
        DEPENDENCY
    }

    private val JSON = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun loadJSConfigs() = runSafeIOJob {
        PathManager.JSConfigs.listFiles { file ->
            file.isDirectory
        }?.mapNotNull {
            val uuid = it.name
            val file = it.asParentOf(FILE_NAME_JS_CONFIG)
            if (file.exists()) {
                val config = file.source().useBuffer {
                    try {
                        readJSON<JSConfig>(JSON)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                if (config == null || config.id != uuid) {
                    file.parentFile?.deleteRecursively()
                    null
                } else {
                    config
                }
            } else {
                file.parentFile?.deleteRecursively()
                null
            }
        }?.map {
            if (!checkLocalJSConfigFiles(it)) {
                deleteJSConfigFiles(it.id, true)
            }
            it
        } ?: emptyList()
    }

    suspend fun checkLocalJSConfigFiles(config: JSConfig) = runOnlyResultIOJob {
        PathManager.JSConfigs.asParentOf(config.id).walkTopDown().forEach {
            if (it.isFile && EXTENSION_DOWNLOAD.equals(it.extension, true)) it.deleteOnExit()
        }
        if (getJSProviderFile(config.id, false).exists() && getJSParserFile(config.id, false).exists()) {
            val jsDependencies = config.dependenciesJSUrls.map { it.md5() }
            val dependenciesDir = getJSDependenciesDir(config.id)
            val localJSDependencies =
                if (dependenciesDir.exists()) {
                    dependenciesDir.listFiles { file -> file.isFile }?.map { it.nameWithoutExtension } ?: emptyList()
                } else {
                    emptyList()
                }
            jsDependencies.subtract(localJSDependencies).isEmpty() && localJSDependencies.subtract(jsDependencies).isEmpty()
        } else {
            false
        }
    }

    suspend fun downloadJS(httpClient: HttpClient, uuid: String, url: String, errorType: JSConfigException.Error, saveType: SaveType) = runUnsafeIOJob {
        try {
            httpClient.get<InputStream>(url).source().use {
                val result = when (saveType) {
                    PROVIDER -> writeJSProvider(uuid, it)
                    PARSER -> writeJSParser(uuid, it)
                    DEPENDENCY -> writeJSDependencies(uuid, url, it)
                }
                if (!result) errorType.make()
            }
        } catch (e: JSConfigException) {
            throw e
        } catch (e: Exception) {
            errorType.report(e)
        }
    }

    private fun getJSProviderFile(uuid: String, download: Boolean) =
        PathManager.JSConfigs.asParentOf(uuid, DIR_SRC, if (download) "$FILE_NAME_JS_PROVIDER.$EXTENSION_DOWNLOAD" else FILE_NAME_JS_PROVIDER)

    suspend fun readJSProvider(uuid: String) = runSafeIOJob {
        getJSProviderFile(uuid, false).source().useBuffer {
            readUtf8()
        }.takeIf {
            it.isNotBlank()
        }
    }

    private suspend fun writeJSProvider(uuid: String, source: Source) = runOnlyResultIOJob {
        getJSProviderFile(uuid, true).withPreparedDir {
            it.sink().useBuffer {
                writeAll(source)
            }
            val jsFile = getJSProviderFile(uuid, false)
            if (jsFile.exists()) jsFile.delete()
            it.renameTo(jsFile)
        } ?: false
    }

    private fun getJSParserFile(uuid: String, download: Boolean) =
        PathManager.JSConfigs.asParentOf(uuid, DIR_SRC, if (download) "$FILE_NAME_JS_PARSER.$EXTENSION_DOWNLOAD" else FILE_NAME_JS_PARSER)

    suspend fun readJSParser(uuid: String) = runSafeIOJob {
        getJSParserFile(uuid, false).source().useBuffer {
            readUtf8()
        }.takeIf {
            it.isNotBlank()
        }
    }

    private suspend fun writeJSParser(uuid: String, source: Source) = runOnlyResultIOJob {
        getJSParserFile(uuid, true).withPreparedDir {
            it.sink().useBuffer {
                writeAll(source)
            }
            val jsFile = getJSParserFile(uuid, false)
            if (jsFile.exists()) jsFile.delete()
            it.renameTo(jsFile)
        } ?: false
    }

    private fun getJSDependenciesDir(uuid: String) = PathManager.JSConfigs.asParentOf(uuid, DIR_LIB)

    suspend fun readJSDependencies(uuid: String) = runSafeIOJob {
        getJSDependenciesDir(uuid).listFiles { file ->
            file.isFile && EXTENSION_JS.equals(file.extension, true)
        }?.mapNotNull {
            it.source().useBuffer {
                readUtf8()
            }.takeIf { str ->
                str.isNotBlank()
            }
        } ?: emptyList()
    }

    private suspend fun writeJSDependencies(uuid: String, url: String, source: Source) = runOnlyResultIOJob {
        val fileName = url.md5()
        val downloadFile = getJSDependenciesDir(uuid).asParentOf("$fileName.$EXTENSION_JS.$EXTENSION_DOWNLOAD")
        val jsFile = getJSDependenciesDir(uuid).asParentOf("$fileName.$EXTENSION_JS")
        downloadFile.withPreparedDir {
            it.sink().useBuffer {
                writeAll(source)
            }
            if (jsFile.exists()) jsFile.delete()
            it.renameTo(jsFile)
        } ?: false
    }

    suspend fun parserJSConfig(content: String) = runUnsafeIOJob {
        try {
            JSON.decodeFromString<JSConfig>(content)
        } catch (e: Exception) {
            JSConfigException.Error.INVALID.report(e)
        }
    }

    suspend fun addNewJSConfig(jsConfig: JSConfig) = runOnlyResultIOJob {
        deleteJSConfigFiles(jsConfig.id, false)
        PathManager.JSConfigs.asParentOf(jsConfig.id, FILE_NAME_JS_CONFIG).withPreparedDir {
            it.sink().useBuffer {
                writeJSON(JSON, jsConfig)
                true
            }
        } ?: false
    }

    suspend fun deleteJSConfigFiles(uuid: String, keepConfig: Boolean) = runSimpleIOJob {
        PathManager.JSConfigs.asParentOf(uuid).takeIfExists()?.let {
            if (keepConfig) {
                PathManager.JSConfigs.asParentOf(uuid, DIR_SRC).takeIfExists()?.deleteRecursively()
                getJSDependenciesDir(uuid).takeIfExists()?.deleteRecursively()
            } else {
                it.deleteRecursively()
            }
        }
    }
}