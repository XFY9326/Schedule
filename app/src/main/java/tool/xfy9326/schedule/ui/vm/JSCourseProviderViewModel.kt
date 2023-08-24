package tool.xfy9326.schedule.ui.vm

import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.postEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.js.JSCourseParser
import tool.xfy9326.schedule.content.js.JSCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.data.AppSettingsDataStore
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
                    val isDebug = AppSettingsDataStore.enableWebCourseProviderConsoleDebugFlow.first()
                    jsContent.postEvent(JSBridge.buildJSCourseProviderJS(isCurrentSchedule, it, isDebug))
                })
        }
    }

    override suspend fun onImportCourse(
        importParams: String,
        importOption: Int,
        courseProvider: JSCourseProvider,
        courseParser: JSCourseParser,
    ): ScheduleImportContent {
        val resultJSON = try {
            resultJSONFormat.decodeFromString<JSBridge.JSProviderResponse>(importParams)
        } catch (e: Exception) {
            CourseAdapterException.Error.JS_RESULT_PARSE_ERROR.report(e)
        }
        if (resultJSON.isSuccess) {
            return courseParser.processJSResult(resultJSON.data)
        } else {
            CourseAdapterException.Error.JS_RUN_FAILED.report(msg = resultJSON.data)
        }
    }
}