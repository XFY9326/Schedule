@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ui.vm.base

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.CourseAdapterManager.newConfigInstance
import tool.xfy9326.schedule.content.base.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.utils.CourseUtils
import tool.xfy9326.schedule.utils.ScheduleUtils
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

abstract class CourseProviderViewModel<I, T1 : AbstractCourseProvider<*>, T2 : AbstractCourseParser<*>> : AbstractViewModel() {
    protected lateinit var internalImportConfig: CourseImportConfig<*, T1, *, T2>
        private set
    private lateinit var _courseProvider: T1
    private lateinit var _courseParser: T2

    protected val courseProvider
        get() = _courseProvider
    protected val courseParser
        get() = _courseParser

    val providerError = MutableEventLiveData<CourseAdapterException>()
    val courseImportFinish = MutableEventLiveData<ImportResult>()

    private val loginParamsLock = Mutex()
    private var importCourseJob: Job? = null

    protected val internalIsImportingCourses = AtomicBoolean(false)

    val isImportingCourses
        get() = internalIsImportingCourses.get()
    val importConfig
        get() = internalImportConfig

    fun registerConfig(config: Class<CourseImportConfig<*, T1, *, T2>>) {
        if (!::internalImportConfig.isInitialized) {
            internalImportConfig = config.newConfigInstance()
            _courseProvider = internalImportConfig.newProvider()
            _courseParser = internalImportConfig.newParser()
        }
    }

    protected abstract suspend fun onImportCourse(
        importParams: I,
        importOption: Int,
        courseProvider: T1,
        courseParser: T2,
    ): ImportContent

    protected fun providerFunctionRunner(
        mutex: Mutex? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        onRun: suspend (T1) -> Unit,
        onFailed: (suspend () -> Unit)? = null,
    ) {
        viewModelScope.launch(dispatcher) {
            if (mutex == null || mutex.tryLock()) {
                try {
                    onRun(_courseProvider)
                } catch (e: CourseAdapterException) {
                    reportError(e, false)
                } catch (e: Exception) {
                    onFailed?.invoke()
                    reportError(CourseAdapterException.Error.UNKNOWN_ERROR.make(e), false)
                } finally {
                    mutex?.unlock()
                }
            }
        }
    }

    fun importCourse(importParams: I, importOption: Int, currentSchedule: Boolean, newScheduleName: String? = null) {
        if (internalIsImportingCourses.compareAndSet(false, true)) {
            importCourseJob = viewModelScope.launch(Dispatchers.Default) {
                try {
                    val content = onImportCourse(importParams, importOption, _courseProvider, _courseParser)

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

                    if (currentSchedule) {
                        ScheduleUtils.saveCurrentSchedule(content.scheduleTimes, courses)
                    } else {
                        ScheduleUtils.saveNewSchedule(newScheduleName, content.scheduleTimes, courses)
                    }

                    reportFinishResult(true, hasConflicts)
                } catch (e: CourseAdapterException) {
                    reportError(e, true)
                } catch (e: Exception) {
                    if (e is SocketTimeoutException || e is SocketException || e is ConnectException || e is UnknownHostException) {
                        reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e), true)
                    } else if (e !is CancellationException) {
                        reportError(CourseAdapterException.Error.UNKNOWN_ERROR.make(e), true)
                    }
                } finally {
                    internalIsImportingCourses.set(false)
                    importCourseJob = null
                }
            }
        }
    }

    protected fun reportError(err: CourseAdapterException, isImportFinish: Boolean) {
        if (isImportFinish) reportFinishResult(false)
        providerError.postEvent(err)
    }

    protected fun reportFinishResult(isSuccess: Boolean, hasConflicts: Boolean = false) {
        courseImportFinish.postEvent(ImportResult(isSuccess, hasConflicts))
    }

    fun finishImport() {
        importCourseJob?.cancel()
        internalIsImportingCourses.set(false)
    }

    protected class ImportContent(val scheduleTimes: List<ScheduleTime>, val coursesParserResult: CourseParseResult)

    class ImportResult(val isSuccess: Boolean, val hasConflicts: Boolean)

    override fun onCleared() {
        finishImport()
    }
}