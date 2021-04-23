package tool.xfy9326.schedule.ui.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.beans.WebCourseImportParams
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.ui.activity.base.CourseProviderActivity
import tool.xfy9326.schedule.ui.vm.base.AbstractWebCourseProviderViewModel

class WebCourseProviderViewModel : AbstractWebCourseProviderViewModel<WebCourseImportParams, WebCourseProvider<*>, WebCourseParser<*>>() {
    val validateHtmlPage = MutableEventLiveData<CourseProviderActivity.ImportRequestParams<WebCourseImportParams>?>()
    private val validatePageLock = Mutex()

    override val initPageUrl = courseProvider.initPageUrl

    fun validateHtmlPage(importParams: WebCourseImportParams, isCurrentSchedule: Boolean) {
        if (!isImportingCourses) {
            providerFunctionRunner(validatePageLock, Dispatchers.Default,
                onRun = {
                    val pageInfo = it.validateCourseImportPage(
                        importParams.htmlContent,
                        importParams.iframeContent,
                        importParams.frameContent
                    )
                    if (pageInfo.isValidPage) {
                        validateHtmlPage.postEvent(CourseProviderActivity.ImportRequestParams(isCurrentSchedule, importParams, pageInfo.asImportOption))
                    } else {
                        validateHtmlPage.postEvent(null)
                    }
                },
                onFailed = {
                    validateHtmlPage.postEvent(null)
                }
            )
        }
    }

    override suspend fun onImportCourse(
        importParams: WebCourseImportParams,
        importOption: Int,
        courseProvider: WebCourseProvider<*>,
        courseParser: WebCourseParser<*>,
    ): ImportContent {
        val scheduleTimes = courseParser.loadScheduleTimes(importOption)
        val coursesParseResult = courseParser.parseCourses(
            importOption,
            importParams.htmlContent,
            importParams.iframeContent,
            importParams.frameContent
        )
        return ImportContent(scheduleTimes, coursesParseResult)
    }
}