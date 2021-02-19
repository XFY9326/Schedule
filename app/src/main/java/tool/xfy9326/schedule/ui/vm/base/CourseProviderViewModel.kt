@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ui.vm.base

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.base.BaseCourseProvider
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.content.base.ICourseParser
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

abstract class CourseProviderViewModel<I, P1 : BaseCourseProvider<*>, P2 : ICourseParser> : AbstractViewModel() {
    protected lateinit var internalImportConfig: CourseImportConfig<*, P1, P2>
        private set
    private lateinit var _courseProvider: P1
    private lateinit var _courseParser: P2

    protected val courseProvider
        get() = _courseProvider
    protected val courseParser
        get() = _courseParser

    val providerError = MutableEventLiveData<CourseAdapterException>()

    // Import success / Has conflicts
    val courseImportFinish = MutableEventLiveData<Pair<Boolean, Boolean>>()

    private val loginParamsLock = Mutex()
    private var importCourseJob: Job? = null

    protected val internalIsImportingCourses = AtomicBoolean(false)

    val isImportingCourses
        get() = internalIsImportingCourses.get()
    val importConfig
        get() = internalImportConfig

    fun registerConfig(config: CourseImportConfig<*, P1, P2>) {
        if (!::internalImportConfig.isInitialized || internalImportConfig != config) {
            internalImportConfig = config
            _courseProvider = config.newProvider()
            _courseParser = config.newParser()
        }
    }

    protected abstract suspend fun onImportCourse(
        importParams: I,
        importOption: Int,
        courseProvider: P1,
        courseParser: P2,
    ): Pair<List<ScheduleTime>, List<Course>>

    protected fun providerFunctionRunner(
        mutex: Mutex? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        onRun: suspend (P1) -> Unit,
        onFailed: (suspend () -> Unit)? = null,
    ) {
        viewModelScope.launch(dispatcher) {
            if (mutex == null || mutex.tryLock()) {
                try {
                    onRun(_courseProvider)
                } catch (e: CourseAdapterException) {
                    providerError.postEvent(e)
                } catch (e: Exception) {
                    onFailed?.invoke()
                    providerError.postEvent(CourseAdapterException.ErrorType.UNKNOWN_ERROR.make(e))
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
                    val result = onImportCourse(importParams, importOption, _courseProvider, _courseParser)
                    val scheduleTimes = result.first
                    val courses = result.second

                    if (courses.isEmpty() && !AppSettingsDataStore.allowImportEmptyScheduleFlow.first()) {
                        courseImportFinish.postEvent(false to false)
                        providerError.postEvent(CourseAdapterException.ErrorType.SCHEDULE_COURSE_IMPORT_EMPTY.make())
                        return@launch
                    }

                    val scheduleTimeValid = ScheduleUtils.validateScheduleTime(scheduleTimes)
                    if (!scheduleTimeValid) {
                        courseImportFinish.postEvent(false to false)
                        providerError.postEvent(CourseAdapterException.ErrorType.SCHEDULE_TIMES_ERROR.make())
                        return@launch
                    }

                    val hasConflicts = CourseUtils.solveConflicts(scheduleTimes, courses)

                    if (currentSchedule) {
                        ScheduleUtils.saveCurrentSchedule(scheduleTimes, courses)
                    } else {
                        ScheduleUtils.saveNewSchedule(newScheduleName, scheduleTimes, courses)
                    }

                    courseImportFinish.postEvent(true to hasConflicts)
                } catch (e: CourseAdapterException) {
                    courseImportFinish.postEvent(false to false)
                    providerError.postEvent(e)
                } catch (e: Exception) {
                    courseImportFinish.postEvent(false to false)
                    if (e is SocketTimeoutException || e is SocketException || e is ConnectException || e is UnknownHostException) {
                        providerError.postEvent(CourseAdapterException.ErrorType.CONNECTION_ERROR.make(e))
                    } else if (e !is CancellationException) {
                        providerError.postEvent(CourseAdapterException.ErrorType.UNKNOWN_ERROR.make(e))
                    }
                } finally {
                    internalIsImportingCourses.set(false)
                    importCourseJob = null
                }
            }
        }
    }

    fun finishImport() {
        importCourseJob?.cancel()
        internalIsImportingCourses.set(false)
    }

    override fun onCleared() {
        finishImport()
    }
}