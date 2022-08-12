package tool.xfy9326.schedule.io

import io.github.xfy9326.atools.base.md5
import io.github.xfy9326.atools.io.okio.copyTo
import io.github.xfy9326.atools.io.okio.readText
import io.github.xfy9326.atools.io.okio.readTextAsync
import io.github.xfy9326.atools.io.serialization.json.readJSONAsync
import io.github.xfy9326.atools.io.serialization.json.writeJSON
import io.github.xfy9326.atools.io.utils.asParentOf
import io.github.xfy9326.atools.io.utils.preparedParentFolder
import io.github.xfy9326.atools.io.utils.takeIfExists
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.Source
import okio.sink
import okio.source
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.make
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.io.JSFileManager.SaveType.*
import java.io.File

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

    suspend fun loadJSConfigs(): List<JSConfig>? = withContext(Dispatchers.IO) {
        runCatching {
            PathManager.JSConfigs.listFiles { file ->
                file.isDirectory
            }?.mapNotNull {
                val uuid = it.name
                val file = it.asParentOf(FILE_NAME_JS_CONFIG)
                if (file.exists()) {
                    val config = file.readJSONAsync<JSConfig>(JSON).getOrNull()
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
            }
        }.getOrNull()
    }

    suspend fun checkLocalJSConfigFiles(config: JSConfig): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            PathManager.JSConfigs.asParentOf(config.id).walkTopDown().forEach {
                if (it.isFile && EXTENSION_DOWNLOAD.equals(it.extension, true)) it.deleteOnExit()
            }
            if (getJSProviderFile(config.id, false).exists() && getJSParserFile(config.id, false).exists()) {
                val jsDependencies = config.dependenciesJSUrls.map { it.md5() }.toSet()
                val dependenciesDir = getJSDependenciesDir(config.id)
                val localJSDependencies =
                    if (dependenciesDir.exists()) {
                        dependenciesDir.listFiles { file -> file.isFile }?.map { it.nameWithoutExtension }?.toSet() ?: emptySet()
                    } else {
                        emptySet()
                    }
                jsDependencies.subtract(localJSDependencies).isEmpty() && localJSDependencies.subtract(jsDependencies).isEmpty()
            } else {
                false
            }
        }.getOrNull() ?: false
    }

    suspend fun downloadJS(httpClient: HttpClient, uuid: String, url: String, errorType: JSConfigException.Error, saveType: SaveType) {
        withContext(Dispatchers.IO) {
            try {
                httpClient.get(url).bodyAsChannel().toInputStream().source().use {
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
    }

    private fun getJSProviderFile(uuid: String, download: Boolean): File =
        PathManager.JSConfigs.asParentOf(uuid, DIR_SRC, if (download) "$FILE_NAME_JS_PROVIDER.$EXTENSION_DOWNLOAD" else FILE_NAME_JS_PROVIDER)

    suspend fun readJSProvider(uuid: String): String? =
        getJSProviderFile(uuid, false).readTextAsync()
            .getOrNull()?.takeIf {
                it.isNotBlank()
            }

    private suspend fun writeJSProvider(uuid: String, source: Source): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            getJSProviderFile(uuid, true).preparedParentFolder().let {
                source.copyTo(it.sink())
                val jsFile = getJSProviderFile(uuid, false)
                if (jsFile.exists()) jsFile.delete()
                it.renameTo(jsFile)
            }
        }.isSuccess
    }

    private fun getJSParserFile(uuid: String, download: Boolean) =
        PathManager.JSConfigs.asParentOf(uuid, DIR_SRC, if (download) "$FILE_NAME_JS_PARSER.$EXTENSION_DOWNLOAD" else FILE_NAME_JS_PARSER)

    suspend fun readJSParser(uuid: String): String? =
        getJSParserFile(uuid, false)
            .readTextAsync()
            .getOrNull()?.takeIf {
                it.isNotBlank()
            }

    private suspend fun writeJSParser(uuid: String, source: Source): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            getJSParserFile(uuid, true).preparedParentFolder().let {
                source.copyTo(it.sink())
                val jsFile = getJSParserFile(uuid, false)
                if (jsFile.exists()) jsFile.delete()
                it.renameTo(jsFile)
            }
        }.isSuccess
    }

    private fun getJSDependenciesDir(uuid: String): File = PathManager.JSConfigs.asParentOf(uuid, DIR_LIB)

    suspend fun readJSDependencies(uuid: String): List<String>? = withContext(Dispatchers.IO) {
        runCatching {
            getJSDependenciesDir(uuid).listFiles { file ->
                file.isFile && EXTENSION_JS.equals(file.extension, true)
            }?.mapNotNull {
                it.readText().takeIf { str ->
                    str.isNotBlank()
                }
            }
        }.getOrNull()
    }

    private suspend fun writeJSDependencies(uuid: String, url: String, source: Source): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = url.md5()
            val downloadFile = getJSDependenciesDir(uuid).asParentOf("$fileName.$EXTENSION_JS.$EXTENSION_DOWNLOAD")
            val jsFile = getJSDependenciesDir(uuid).asParentOf("$fileName.$EXTENSION_JS")
            downloadFile.preparedParentFolder().let {
                source.copyTo(it.sink())
                if (jsFile.exists()) jsFile.delete()
                it.renameTo(jsFile)
            }
        }.isSuccess
    }

    suspend fun parserJSConfig(content: String): JSConfig = withContext(Dispatchers.IO) {
        try {
            JSON.decodeFromString(content)
        } catch (e: JSConfigException) {
            throw e
        } catch (e: Exception) {
            JSConfigException.Error.INVALID.report(e)
        }
    }

    suspend fun addNewJSConfig(jsConfig: JSConfig): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            deleteJSConfigFiles(jsConfig.id, false)
            PathManager.JSConfigs.asParentOf(jsConfig.id, FILE_NAME_JS_CONFIG).preparedParentFolder().writeJSON(jsConfig, JSON)
        }.isSuccess
    }

    suspend fun deleteJSConfigFiles(uuid: String, keepConfig: Boolean): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            PathManager.JSConfigs.asParentOf(uuid).takeIfExists()?.let {
                if (keepConfig) {
                    PathManager.JSConfigs.asParentOf(uuid, DIR_SRC).takeIfExists()?.deleteRecursively()
                    getJSDependenciesDir(uuid).takeIfExists()?.deleteRecursively()
                } else {
                    it.deleteRecursively()
                }
            }
        }.getOrNull() ?: false
    }
}