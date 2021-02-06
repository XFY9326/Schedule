package tool.xfy9326.schedule.ui.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.beans.WebCourseImportParams
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel

class WebCourseProviderViewModel : CourseProviderViewModel<WebCourseImportParams, WebCourseProvider<*>, WebCourseParser>() {
    var isBottomPanelInitShowed = false

    val validateHtmlPage = MutableEventLiveData<Triple<Boolean, WebCourseImportParams, Int>?>()
    val initPageUrl
        get() = courseProvider.initPageUrl

    private val validatePageLock = Mutex()

    fun validateHtmlPage(importParams: WebCourseImportParams, isCurrentSchedule: Boolean) {
        providerFunctionRunner(validatePageLock, Dispatchers.Default,
            onRun = {
                val pageInfo = it.validateCourseImportPage(
                    importParams.htmlContent,
                    importParams.iframeContent,
                    importParams.frameContent
                )
                if (pageInfo.isValidPage) {
                    validateHtmlPage.postEvent(Triple(isCurrentSchedule, importParams, pageInfo.asImportOption))
                } else {
                    validateHtmlPage.postEvent(null)
                }
            },
            onFailed = {
                validateHtmlPage.postEvent(null)
            }
        )
    }

    override suspend fun onImportCourse(
        importParams: WebCourseImportParams,
        importOption: Int,
        courseProvider: WebCourseProvider<*>,
        courseParser: WebCourseParser,
    ): Pair<List<ScheduleTime>, List<Course>> {
        val scheduleTimes = courseParser.loadScheduleTimes(importOption)
        val courses = courseParser.parseCourses(
            importOption,
            importParams.htmlContent,
            importParams.iframeContent,
            importParams.frameContent
        )
        return scheduleTimes to courses
    }
}