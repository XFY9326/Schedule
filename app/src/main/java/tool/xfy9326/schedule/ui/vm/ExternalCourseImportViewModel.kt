package tool.xfy9326.schedule.ui.vm

import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
import lib.xfy9326.android.kit.io.kt.readText
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.content.ExternalCourseProcessorRegistry
import tool.xfy9326.schedule.content.beans.ExternalCourseImportData
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager

class ExternalCourseImportViewModel : AbstractViewModel() {
    /**
     * Inject by ExternalCourseImportUtils in ViewModelProvider.NewInstanceFactory
     * @link[tool.xfy9326.schedule.utils.ExternalCourseImportUtils.prepareRunningEnvironment]
     */
    lateinit var importParams: ExternalCourseImportData.Origin

    private val processor by lazy {
        ExternalCourseProcessorRegistry.getProcessor(importParams.processorName) ?: error("External processor not found! Name: ${importParams.processorName}")
    }

    val schoolName
        get() = processor.schoolName
    val authorName
        get() = processor.authorName
    val systemName
        get() = processor.systemName

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
            processor.importCourse(ExternalCourseImportData(
                importParams.fileUri.readText() ?: CourseAdapterException.Error.PARSE_PAGE_ERROR.report(),
                importParams.processorExtraData
            )) ?: CourseAdapterException.Error.PARSE_PAGE_ERROR.report()
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