package tool.xfy9326.schedule.ui.vm

import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.content.base.JSCourseParser
import tool.xfy9326.schedule.content.base.JSCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.ui.vm.base.AbstractWebCourseProviderViewModel
import tool.xfy9326.schedule.utils.JSBridge

class JSCourseProviderViewModel : AbstractWebCourseProviderViewModel<String, JSCourseProvider, JSCourseParser>() {
    private val jsLoadLock = Mutex()
    val jsContent = MutableEventLiveData<String>()

    override val initPageUrl = courseProvider.initPageUrl

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
    ): ImportContent {
        val resultJSON = Json.decodeFromString<JSBridge.JSResult>(importParams)
        if (resultJSON.isSuccess) {
            val result = courseParser.processJSResult(resultJSON.data)
            return ImportContent(result.first, result.second)
        } else {
            CourseAdapterException.Error.JS_HANDLE_ERROR.report(resultJSON.data)
        }
    }
}