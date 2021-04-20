package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.content.beans.JSParams
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.io.JSFileManager

class JSCourseParser : AbstractCourseParser<JSParams>() {
    private var parserCache: String? = null

    val functionName = ""

    suspend fun getJSParser() =
        try {
            parserCache ?: JSFileManager.readJSParser(requireParams().uuid)?.also {
                parserCache = it
            } ?: JSConfigException.Error.READ_FAILED.report()
        } catch (e: JSConfigException) {
            throw e
        } catch (e: Exception) {
            JSConfigException.Error.UNKNOWN_ERROR.report(e)
        }
}