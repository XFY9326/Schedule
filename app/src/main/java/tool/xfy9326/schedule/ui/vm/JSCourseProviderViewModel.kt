package tool.xfy9326.schedule.ui.vm

import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.js.JSCourseParser
import tool.xfy9326.schedule.content.js.JSCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.ui.vm.base.AbstractWebCourseProviderViewModel
import tool.xfy9326.schedule.utils.JSBridge

class JSCourseProviderViewModel : AbstractWebCourseProviderViewModel<String, JSCourseProvider, JSCourseParser>() {
    private val resultJSONFormat = Json { ignoreUnknownKeys = true }
    private val jsLoadLock = Mutex()
    val jsContent = MutableEventLiveData<String>()
    val isRequireNetwork
        get() = courseProvider.isRequireNetwork()

    override fun getInitUrl(): String {
        return courseProvider.getInitUrl()
    }

    fun requestJSContent(isCurrentSchedule: Boolean) {
        if (!isImportingCourses) {
            providerFunctionRunner(jsLoadLock,
                onRun = {
                    jsContent.postEvent(JSBridge.buildJSCourseProviderJS(isCurrentSchedule, it))
                })
        }
    }

    override suspend fun onImportCourse(
        importParams: String,
        importOption: Int,
        courseProvider: JSCourseProvider,
        courseParser: JSCourseParser,
    ): ScheduleImportContent {
        val resultJSON = resultJSONFormat.decodeFromString<JSBridge.JSProviderResponse>(importParams)
        if (resultJSON.isSuccess) {
            return courseParser.processJSResult(resultJSON.data)
        } else {
            CourseAdapterException.Error.JSON_PARSE_ERROR.report()
        }
    }
}