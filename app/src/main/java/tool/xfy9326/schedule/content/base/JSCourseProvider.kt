package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.content.beans.JSParams
import tool.xfy9326.schedule.content.utils.JSConfigException
import tool.xfy9326.schedule.content.utils.JSConfigException.Companion.report
import tool.xfy9326.schedule.io.JSFileManager

class JSCourseProvider : AbstractCourseProvider<JSParams>() {
    private var providerCache: String? = null
    private var dependenciesCache: List<String>? = null

    val functionName = ""

    suspend fun getJSProvider() =
        try {
            providerCache ?: JSFileManager.readJSProvider(requireParams().uuid)?.also {
                providerCache = it
            } ?: JSConfigException.Error.READ_FAILED.report()
        } catch (e: JSConfigException) {
            throw e
        } catch (e: Exception) {
            JSConfigException.Error.UNKNOWN_ERROR.report(e)
        }

    suspend fun getJSDependencies() =
        try {
            dependenciesCache ?: JSFileManager.readJSDependencies(requireParams().uuid)?.also {
                dependenciesCache = it
            } ?: JSConfigException.Error.READ_FAILED.report()
        } catch (e: JSConfigException) {
            throw e
        } catch (e: Exception) {
            JSConfigException.Error.UNKNOWN_ERROR.report(e)
        }
}