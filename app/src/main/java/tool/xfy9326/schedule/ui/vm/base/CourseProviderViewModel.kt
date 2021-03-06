@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ui.vm.base

import androidx.annotation.CallSuper
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.base.*
import tool.xfy9326.schedule.content.beans.CourseImportInstance
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.make
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.utils.schedule.CourseUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class CourseProviderViewModel<I, T1 : AbstractCourseProvider<*>, T2 : AbstractCourseParser<*>> : AbstractViewModel() {
    protected lateinit var internalImportConfigInstance: CourseImportInstance<T1, T2>
        private set

    protected val courseProvider
        get() = internalImportConfigInstance.provider
    protected val courseParser
        get() = internalImportConfigInstance.parser

    val providerError = MutableEventLiveData<CourseAdapterException>()
    val courseImportFinish = MutableEventLiveData<Pair<ImportResult, Long?>>()

    private val loginParamsLock = Mutex()
    private var importCourseJob: Job? = null

    protected val internalIsImportingCourses = AtomicBoolean(false)

    val isImportingCourses
        get() = internalIsImportingCourses.get()
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
                    reportError(e, false)
                } catch (e: Exception) {
                    onFailed?.invoke()
                    reportError(CourseAdapterException.Error.UNKNOWN_ERROR.make(e), false)
                } finally {
                    mutex?.unlock()
                }
            }
            return true
        }
        return false
    }

    fun importCourse(importParams: I, importOption: Int, currentSchedule: Boolean, newScheduleName: String? = null) {
        if (internalIsImportingCourses.compareAndSet(false, true)) {
            importCourseJob = viewModelScope.launch(Dispatchers.Default) {
                try {
                    val content = onImportCourse(importParams, importOption, courseProvider, courseParser)

                    if (content.coursesParserResult.ignorableError != null && !AppSettingsDataStore.allowImportIncompleteScheduleFlow.first()) {
                        reportError(content.coursesParserResult.ignorableError, true)
                        return@launch
                    }

                    val courses = content.coursesParserResult.courses
                    if (courses.isEmpty() && !AppSettingsDataStore.allowImportEmptyScheduleFlow.first()) {
                        reportError(CourseAdapterException.Error.SCHEDULE_COURSE_IMPORT_EMPTY.make(), true)
                        return@launch
                    }

                    val scheduleTimeValid = ScheduleUtils.validateScheduleTime(content.scheduleTimes)
                    if (!scheduleTimeValid) {
                        reportError(CourseAdapterException.Error.SCHEDULE_TIMES_ERROR.make(), true)
                        return@launch
                    }

                    val hasConflicts = CourseUtils.solveConflicts(content.scheduleTimes, courses)

                    val editScheduleId = if (currentSchedule) {
                        ScheduleUtils.saveCurrentSchedule(content.scheduleTimes, courses)
                    } else {
                        ScheduleUtils.saveNewSchedule(newScheduleName, content.scheduleTimes, courses, content.term)
                    }

                    if (hasConflicts) {
                        reportFinishResult(ImportResult.SUCCESS_WITH_IGNORABLE_CONFLICTS, editScheduleId)
                    } else {
                        reportFinishResult(ImportResult.SUCCESS, editScheduleId)
                    }
                } catch (e: CancellationException) {
                    // Ignore
                } catch (e: CourseAdapterException) {
                    reportError(e, true)
                } catch (e: SocketTimeoutException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e), true)
                } catch (e: SocketException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e), true)
                } catch (e: ConnectException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e), true)
                } catch (e: UnknownHostException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e), true)
                } catch (e: Exception) {
                    reportError(CourseAdapterException.Error.UNKNOWN_ERROR.make(e), true)
                } finally {
                    internalIsImportingCourses.set(false)
                    importCourseJob = null
                }
            }
        }
    }

    protected fun reportError(err: CourseAdapterException, isImportFinish: Boolean = false) {
        if (isImportFinish) reportFinishResult(ImportResult.FAILED)
        providerError.postEvent(err)
    }

    protected fun reportFinishResult(result: ImportResult, scheduleId: Long? = null) {
        courseImportFinish.postEvent(result to scheduleId)
    }

    fun finishImport() {
        importCourseJob?.cancel()
        internalIsImportingCourses.set(false)
    }

    enum class ImportResult {
        SUCCESS,
        FAILED,
        SUCCESS_WITH_IGNORABLE_CONFLICTS
    }

    @CallSuper
    override fun onCleared() {
        finishImport()
        onProviderDestroy()
    }

    protected open fun onProviderDestroy() {}
}