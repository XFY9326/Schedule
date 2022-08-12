@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.content.base

import io.ktor.client.*
import kotlinx.coroutines.cancel
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import java.io.Serializable

abstract class NetworkCourseProvider<P : Serializable> : AbstractCourseProvider<P>() {
    private lateinit var internalHttpClient: HttpClient
    protected val httpClient
        get() = internalHttpClient


    fun init() {
        try {
            internalHttpClient = onPrepareClient()
        } catch (e: Exception) {
            CourseAdapterException.Error.INIT_ERROR.report(e)
        }
    }

    suspend fun loadImportOptions() = try {
        onLoadImportOptions()
    } catch (e: Exception) {
        CourseAdapterException.Error.IMPORT_OPTION_GET_ERROR.report(e)
    }

    suspend fun loadScheduleTimesHtml(importOption: Int) = onLoadScheduleTimesHtml(importOption)

    suspend fun loadCoursesHtml(importOption: Int) = onLoadCoursesHtml(importOption)

    suspend fun loadTermHtml(importOption: Int) = onLoadTermHtml(importOption)

    suspend fun clearConnection() {
        try {
            onClearConnection()
        } catch (e: Exception) {
            CourseAdapterException.Error.CLEAR_ERROR.report(e)
        }
    }

    fun close() {
        try {
            internalHttpClient.cancel()
            internalHttpClient.close()
        } catch (e: CourseAdapterException) {
            throw e
        } catch (e: Exception) {
            CourseAdapterException.Error.CLOSE_ERROR.report(e)
        }
    }


    protected open fun onPrepareClient(): HttpClient = CourseAdapterUtils.buildSimpleHttpClient()

    protected open suspend fun onLoadImportOptions(): Array<String>? = null

    protected open suspend fun onLoadScheduleTimesHtml(importOption: Int): String? = null

    protected open suspend fun onLoadTermHtml(importOption: Int): String? = null

    protected abstract suspend fun onLoadCoursesHtml(importOption: Int): String

    protected open suspend fun onClearConnection() {}
}