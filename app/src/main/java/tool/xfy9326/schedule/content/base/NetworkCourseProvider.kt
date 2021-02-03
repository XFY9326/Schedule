@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import io.ktor.client.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import java.io.Serializable

/**
 * Network course provider
 * 注：所有方法可能并非只执行一次，因此不建议使用全局变量传递参数
 *
 * @constructor Create empty Network course provider
 */
abstract class NetworkCourseProvider<P : Serializable>(params: P?) : BaseCourseProvider<P>(params) {
    private var httpClient: HttpClient? = null

    suspend fun init() {
        try {
            httpClient = onPrepareClient()
        } catch (e: Exception) {
            CourseAdapterException.ErrorType.INIT_ERROR.report(e)
        }
    }

    fun requireHttpClient() = httpClient!!

    suspend fun loadImportOptions() = try {
        onLoadImportOptions(requireHttpClient())
    } catch (e: Exception) {
        CourseAdapterException.ErrorType.IMPORT_OPTION_GET_ERROR.report(e)
    }

    suspend fun loadScheduleTimesHtml(importOption: Int) = onLoadScheduleTimesHtml(requireHttpClient(), importOption)

    suspend fun loadCoursesHtml(importOption: Int) = onLoadCoursesHtml(requireHttpClient(), importOption)

    suspend fun close() {
        try {
            onClearConnection(requireHttpClient())
            httpClient?.close()
        } catch (e: Exception) {
            CourseAdapterException.ErrorType.CLOSE_ERROR.report(e)
        }
    }

    /**
     * Load import options
     * If you have already set importOptions in CourseImportConfig, this would be useless
     *
     * @param httpClient Ktor HttpClient
     * @return
     */
    protected open suspend fun onLoadImportOptions(httpClient: HttpClient): Array<String>? = null

    /**
     * Prepare client
     *
     * @return
     */
    protected open suspend fun onPrepareClient(): HttpClient = CourseAdapterUtils.getDefaultHttpClient()

    /**
     * Load schedule times html
     *
     * @param httpClient Ktor HttpClient
     * @param importOption Import option, Default: 0
     * @return
     */
    protected open suspend fun onLoadScheduleTimesHtml(httpClient: HttpClient, importOption: Int): String? = null

    /**
     * Load courses html
     *
     * @param httpClient Ktor HttpClient
     * @param importOption Import option, Default: 0
     * @return
     */
    protected abstract suspend fun onLoadCoursesHtml(httpClient: HttpClient, importOption: Int): String

    /**
     * Clear connection
     *
     * @param httpClient Ktor HttpClient
     */
    protected open suspend fun onClearConnection(httpClient: HttpClient) {}
}