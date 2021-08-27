package tool.xfy9326.schedule.ui.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.beans.ScheduleImportRequestParams
import tool.xfy9326.schedule.beans.WebPageContent
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.utils.CourseImportHelper
import tool.xfy9326.schedule.ui.vm.base.AbstractWebCourseProviderViewModel

class WebCourseProviderViewModel : AbstractWebCourseProviderViewModel<WebPageContent, WebCourseProvider<*>, WebCourseParser<*>>() {
    val validateHtmlPage = MutableEventLiveData<ScheduleImportRequestParams<WebPageContent>?>()
    private val validatePageLock = Mutex()

    override fun getInitUrl(): String {
        return courseProvider.initPageUrl
    }

    fun validateHtmlPage(importParams: WebPageContent, isCurrentSchedule: Boolean) {
        if (!isImportingCourses) {
            providerFunctionRunner(validatePageLock, Dispatchers.Default,
                onRun = {
                    val result = CourseImportHelper.analyseWebPage(importParams, it)
                    if (result != null) {
                        validateHtmlPage.postEvent(ScheduleImportRequestParams(
                            isCurrentSchedule,
                            result.second,
                            result.first
                        ))
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
        importParams: WebPageContent,
        importOption: Int,
        courseProvider: WebCourseProvider<*>,
        courseParser: WebCourseParser<*>,
    ): ScheduleImportContent = CourseImportHelper.parseWebCourse(importParams, importOption, courseParser)
}