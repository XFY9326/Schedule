package tool.xfy9326.schedule.ui.vm

import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.io.okio.readText
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.postEvent
import tool.xfy9326.schedule.content.ExternalCourseProcessorRegistry
import tool.xfy9326.schedule.content.beans.ExternalCourseImportData
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseImportHelper
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager

class ExternalCourseImportViewModel : AbstractViewModel() {
    /**
     * Inject by ExternalCourseImportUtils in ViewModelProvider.NewInstanceFactory
     * @link[tool.xfy9326.schedule.utils.schedule.ExternalCourseImportUtils.prepareRunningEnvironment]
     */
    lateinit var importParams: ExternalCourseImportData.Origin

    private val processor by lazy {
        val params = importParams
        if (params is ExternalCourseImportData.Origin.External) {
            ExternalCourseProcessorRegistry.getProcessor(params.processorName) ?: error("External processor not found! Name: ${params.processorName}")
        } else {
            error("Params type error!")
        }
    }

    // Only available when using ExternalCourseImportData.Origin.External
    val adapterInfo
        get() = processor.adapterInfo

    var isInit = true
    var isLoading = false
    var isSuccess = false

    private val scheduleImportManager = ScheduleImportManager().apply {
        setOnErrorListener(::reportError)
        setOnFinishListener(::reportFinishResult)
    }

    val providerError = MutableEventLiveData<CourseAdapterException>()
    val courseImportFinish = MutableEventLiveData<Pair<ScheduleImportManager.ImportResult, Long?>>()

    val isImportingCourses
        get() = scheduleImportManager.isImportingCourses

    fun importCourse(currentSchedule: Boolean, newScheduleName: String? = null) {
        scheduleImportManager.importCourse(viewModelScope, currentSchedule, newScheduleName) {
            val params = importParams
            val fileContentList = params.fileUriList.map {
                try {
                    it.readText()
                } catch (e: Exception) {
                    CourseAdapterException.Error.PARSE_PAGE_ERROR.report(msg = "Failed to read $it")
                }
            }
            when (params) {
                is ExternalCourseImportData.Origin.External ->
                    processor.importCourse(ExternalCourseImportData(fileContentList, params.processorExtraData)) ?: CourseAdapterException.Error.PARSE_PAGE_ERROR.report()
                is ExternalCourseImportData.Origin.JSON ->
                    CourseImportHelper.parsePureScheduleJSON(fileContentList.first(), params.combineCourse, params.combineCourseTime)
            }
        }
    }

    private fun reportError(err: CourseAdapterException) {
        providerError.postEvent(err)
    }

    private fun reportFinishResult(result: ScheduleImportManager.ImportResult, scheduleId: Long? = null) {
        courseImportFinish.postEvent(result to scheduleId)
    }

    fun finishImport() = scheduleImportManager.finishImport()

    @CallSuper
    override fun onCleared() {
        scheduleImportManager.finishImport()
    }
}