@file:Suppress("BlockingMethodInNonBlockingContext")

package tool.xfy9326.schedule.io

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.sink
import okio.source
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.content.utils.md5
import tool.xfy9326.schedule.io.kt.*

/**
 * |----- config.json
 * |----- src
 *        |----- ScheduleProvider.js
 *        |----- ScheduleParser.js
 * |----- lib
 *        |-----[Url MD5].js
 */
object JSFileManager {
    private const val FILE_NAME_JS_CONFIG = "config.json"
    private const val FILE_NAME_JS_PROVIDER = "provider.js"
    private const val FILE_NAME_JS_PARSER = "parser.js"

    private const val DIR_SRC = "src"
    private const val DIR_LIB = "lib"

    private const val EXTENSION_JS = ".js"

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
                if (config == null || config.uuid != uuid) {
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
                deleteJSConfigFiles(it.uuid, true)
            }
            it
        } ?: emptyList()
    }

    suspend fun checkLocalJSConfigFiles(config: JSConfig) = runOnlyResultIOJob {
        if (getJSProviderFile(config.uuid).exists() && getJSParserFile(config.uuid).exists()) {
            val jsDependencies = config.dependenciesJSUrls.map { it.md5() }
            val dependenciesDir = getJSDependenciesDir(config.uuid)
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

    private fun getJSProviderFile(uuid: String) = PathManager.JSConfigs.asParentOf(uuid, DIR_SRC, FILE_NAME_JS_PROVIDER)

    suspend fun readJSProvider(uuid: String) = runSafeIOJob {
        getJSProviderFile(uuid).source().useBuffer {
            readUtf8()
        }.takeIf {
            it.isNotBlank()
        }
    }

    suspend fun writeJSProvider(uuid: String, content: String) = runOnlyResultIOJob {
        getJSProviderFile(uuid).withPreparedDir {
            it.sink().useBuffer {
                writeUtf8(content)
                true
            }
        } ?: false
    }

    private fun getJSParserFile(uuid: String) = PathManager.JSConfigs.asParentOf(uuid, DIR_SRC, FILE_NAME_JS_PARSER)

    suspend fun readJSParser(uuid: String) = runSafeIOJob {
        getJSParserFile(uuid).source().useBuffer {
            readUtf8()
        }.takeIf {
            it.isNotBlank()
        }
    }

    suspend fun writeJSParser(uuid: String, content: String) = runOnlyResultIOJob {
        getJSParserFile(uuid).withPreparedDir {
            it.sink().useBuffer {
                writeUtf8(content)
                true
            }
        } ?: false
    }

    private fun getJSDependenciesDir(uuid: String) = PathManager.JSConfigs.asParentOf(uuid, DIR_LIB)

    suspend fun readJSDependencies(uuid: String) = runSafeIOJob {
        getJSDependenciesDir(uuid).listFiles { file ->
            file.isFile
        }?.mapNotNull {
            it.source().useBuffer {
                readUtf8()
            }.takeIf { str ->
                str.isNotBlank()
            }
        } ?: emptyList()
    }

    suspend fun writeJSDependencies(uuid: String, url: String, content: String) = runOnlyResultIOJob {
        getJSDependenciesDir(uuid).asParentOf(url.md5() + EXTENSION_JS).withPreparedDir {
            it.sink().useBuffer {
                writeUtf8(content)
                true
            }
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
        deleteJSConfigFiles(jsConfig.uuid, false)
        PathManager.JSConfigs.asParentOf(jsConfig.uuid, FILE_NAME_JS_CONFIG).withPreparedDir {
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