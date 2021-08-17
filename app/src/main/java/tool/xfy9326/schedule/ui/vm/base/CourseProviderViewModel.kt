@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ui.vm.base

import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.beans.CourseImportInstance
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.make
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager

abstract class CourseProviderViewModel<I, T1 : AbstractCourseProvider<*>, T2 : AbstractCourseParser<*>> : AbstractViewModel() {
    protected lateinit var internalImportConfigInstance: CourseImportInstance<T1, T2>
        private set

    protected val courseProvider
        get() = internalImportConfigInstance.provider
    protected val courseParser
        get() = internalImportConfigInstance.parser

    val providerError = MutableEventLiveData<CourseAdapterException>()
    val courseImportFinish = MutableEventLiveData<Pair<ScheduleImportManager.ImportResult, Long?>>()

    private val scheduleImportManager = ScheduleImportManager().apply {
        setOnErrorListener(::reportError)
        setOnFinishListener(::reportFinishResult)
    }

    val isImportingCourses
        get() = scheduleImportManager.isImportingCourses
    val importConfigInstance
        get() = internalImportConfigInstance

    fun registerConfig(config: AbstractCourseImportConfig<*, T1, *, T2>) {
        if (!::internalImportConfigInstance.isInitialized) {
            internalImportConfigInstance = config.getInstance()
            onProviderCreate()
        }
    }

    protected open fun onProviderCreate() {}

    protected abstract suspend fun onImportCourse(
        importParams: I,
        importOption: Int,
        courseProvider: T1,
        courseParser: T2,
    ): ScheduleImportContent

    protected fun providerFunctionRunner(
        mutex: Mutex? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        onRun: suspend (T1) -> Unit,
        onFailed: (suspend () -> Unit)? = null,
    ): Boolean {
        if (mutex == null || mutex.tryLock()) {
            viewModelScope.launch(dispatcher) {
                try {
                    onRun(courseProvider)
                } catch (e: CourseAdapterException) {
                    onFailed?.invoke()
                    reportError(e)
                } catch (e: Exception) {
                    onFailed?.invoke()
                    reportError(CourseAdapterException.Error.UNKNOWN_ERROR.make(e))
                } finally {
                    mutex?.unlock()
                }
            }
            return true
        }
        return false
    }

    fun importCourse(importParams: I, importOption: Int, currentSchedule: Boolean, newScheduleName: String? = null) {
        scheduleImportManager.importCourse(viewModelScope, currentSchedule, newScheduleName) {
            onImportCourse(importParams, importOption, courseProvider, courseParser)
        }
    }

    protected fun reportError(err: CourseAdapterException) {
        providerError.postEvent(err)
    }

    protected fun reportFinishResult(result: ScheduleImportManager.ImportResult, scheduleId: Long? = null) {
        courseImportFinish.postEvent(result to scheduleId)
    }

    fun finishImport() = scheduleImportManager.finishImport()

    @CallSuper
    override fun onCleared() {
        scheduleImportManager.finishImport()
        onProviderDestroy()
    }

    protected open fun onProviderDestroy() {}
}