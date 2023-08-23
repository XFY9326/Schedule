package tool.xfy9326.schedule.content.js

import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.content.beans.JSParams
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.make
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.io.JSFileManager

class JSCourseProvider : AbstractCourseProvider<JSParams>() {
    private var parserCache: String? = null
    private var providerCache: String? = null
    private var dependenciesCache: List<String>? = null

    fun getInitUrl() = requireParams().initUrl

    fun isRequireNetwork() = requireParams().requireNetwork

    fun isAsyncEnvironment() = requireParams().asyncEnvironment

    fun getProviderFunctionName(): String = when (requireParams().jsType) {
        JSConfig.TYPE_AI_SCHEDULE -> "scheduleHtmlProvider"
        JSConfig.TYPE_PURE_SCHEDULE -> "pureScheduleProvider"
        else -> error("Unsupported JS Type! ${requireParams().jsType}")
    }

    fun getParserFunctionName(): String = when (requireParams().jsType) {
        JSConfig.TYPE_AI_SCHEDULE -> "scheduleHtmlParser"
        JSConfig.TYPE_PURE_SCHEDULE -> "pureScheduleParser"
        else -> error("Unsupported JS Type! ${requireParams().jsType}")
    }

    fun getProviderFunctionCallGenerator(functionName: String): (htmlParam: String, iframeListParam: String, frameListParam: String) -> String {
        return when (requireParams().jsType) {
            // String String Document
            JSConfig.TYPE_AI_SCHEDULE -> {
                { _, p1, p2 -> "${functionName}(${p1.trim()}.join(\"\"), ${p2.trim()}.join(\"\"), document)" }
            }
            // String String[] String[]
            JSConfig.TYPE_PURE_SCHEDULE -> {
                { p0, p1, p2 -> "${functionName}(${p0.trim()}, ${p1.trim()}, ${p2.trim()})" }
            }

            else -> error("Unsupported JS Type! ${requireParams().jsType}")
        }
    }

    fun getParserFunctionCallGenerator(functionName: String): (html: String) -> String {
        return when (requireParams().jsType) {
            // String
            JSConfig.TYPE_AI_SCHEDULE -> {
                { "${functionName}(${it.trim()})" }
            }
            // String
            JSConfig.TYPE_PURE_SCHEDULE -> {
                { "${functionName}(${it.trim()})" }
            }

            else -> error("Unsupported JS Type! ${requireParams().jsType}")
        }
    }

    suspend fun getJSParser() = runJSLoad { uuid ->
        parserCache ?: JSFileManager.readJSParser(uuid)?.also {
            parserCache = it
        }
    }

    suspend fun getJSProvider() = runJSLoad { uuid ->
        providerCache ?: JSFileManager.readJSProvider(uuid)?.also {
            providerCache = it
        }
    }

    suspend fun getJSDependencies() = runJSLoad { uuid ->
        dependenciesCache ?: JSFileManager.readJSDependencies(uuid)?.also {
            dependenciesCache = it
        }
    }

    private suspend fun <T> runJSLoad(block: suspend (String) -> T?): T =
        try {
            block(requireParams().uuid) ?: JSConfigException.Error.READ_FAILED.report()
        } catch (e: JSConfigException) {
            CourseAdapterException.Error.JS_LOAD_ERROR.report(e)
        } catch (e: Exception) {
            CourseAdapterException.Error.JS_LOAD_ERROR.report(JSConfigException.Error.UNKNOWN_ERROR.make(e))
        }
}