package tool.xfy9326.schedule.content.base

import io.ktor.client.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import java.io.Serializable

abstract class NetworkCourseProvider<P : Serializable> : AbstractCourseProvider<P>() {
    private var httpClient: HttpClient? = null

    suspend fun init() {
        try {
            httpClient = onPrepareClient()
        } catch (e: Exception) {
            CourseAdapterException.Error.INIT_ERROR.report(e)
        }
    }

    fun requireHttpClient() = httpClient!!

    suspend fun loadImportOptions() = try {
        onLoadImportOptions(requireHttpClient())
    } catch (e: Exception) {
        CourseAdapterException.Error.IMPORT_OPTION_GET_ERROR.report(e)
    }

    suspend fun loadScheduleTimesHtml(importOption: Int) = onLoadScheduleTimesHtml(requireHttpClient(), importOption)

    suspend fun loadCoursesHtml(importOption: Int) = onLoadCoursesHtml(requireHttpClient(), importOption)

    suspend fun close() {
        try {
            onClearConnection(requireHttpClient())
            httpClient?.close()
        } catch (e: Exception) {
            CourseAdapterException.Error.CLOSE_ERROR.report(e)
        }
    }

    protected open suspend fun onLoadImportOptions(httpClient: HttpClient): Array<String>? = null

    protected open suspend fun onPrepareClient(): HttpClient = CourseAdapterUtils.getDefaultHttpClient()

    protected open suspend fun onLoadScheduleTimesHtml(httpClient: HttpClient, importOption: Int): String? = null

    protected abstract suspend fun onLoadCoursesHtml(httpClient: HttpClient, importOption: Int): String

    protected open suspend fun onClearConnection(httpClient: HttpClient) {}
}