package tool.xfy9326.schedule.utils.schedule

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.make
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

class ScheduleImportManager {
    private var importCourseJob: Job? = null
    private var onError: ((CourseAdapterException) -> Unit)? = null
    private var onFinish: ((ImportResult, Long?) -> Unit)? = null
    private val internalIsImportingCourses = AtomicBoolean(false)

    val isImportingCourses
        get() = internalIsImportingCourses.get()

    fun setOnErrorListener(listener: (CourseAdapterException) -> Unit) {
        onError = listener
    }

    fun setOnFinishListener(listener: (ImportResult, Long?) -> Unit) {
        onFinish = listener
    }

    fun importCourse(
        scope: CoroutineScope,
        currentSchedule: Boolean,
        newScheduleName: String?,
        onProcess: suspend () -> ScheduleImportContent,
    ) {
        if (internalIsImportingCourses.compareAndSet(false, true)) {
            importCourseJob = scope.launch(Dispatchers.Default) {
                try {
                    val content = onProcess()

                    if (content.coursesParserResult.ignorableError != null && !AppSettingsDataStore.allowImportIncompleteScheduleFlow.first()) {
                        reportError(content.coursesParserResult.ignorableError)
                        return@launch
                    }

                    val courses = content.coursesParserResult.courses
                    if (courses.isEmpty() && !AppSettingsDataStore.allowImportEmptyScheduleFlow.first()) {
                        reportError(CourseAdapterException.Error.SCHEDULE_COURSE_IMPORT_EMPTY.make())
                        return@launch
                    }

                    val scheduleTimeErrorType = ScheduleUtils.validateScheduleTime(content.scheduleTimes)
                    if (scheduleTimeErrorType != null) {
                        reportError(CourseAdapterException.Error.SCHEDULE_TIMES_ERROR.make(msg = scheduleTimeErrorType.errorType.name))
                        return@launch
                    }

                    val hasConflicts = CourseUtils.solveConflicts(content.scheduleTimes, courses)

                    val editScheduleId = if (currentSchedule) {
                        ScheduleUtils.saveCurrentSchedule(content.scheduleTimes, courses)
                    } else {
                        ScheduleUtils.saveNewSchedule(newScheduleName, content.scheduleTimes, courses, content.term)
                    }

                    if (hasConflicts) {
                        onFinish?.invoke(ImportResult.SUCCESS_WITH_IGNORABLE_CONFLICTS, editScheduleId)
                    } else {
                        onFinish?.invoke(ImportResult.SUCCESS, editScheduleId)
                    }

                    if (!AppDataStore.hasCurrentScheduleId() || !currentSchedule && AppSettingsDataStore.autoSwitchToNewImportScheduleFlow.first()) {
                        AppDataStore.setCurrentScheduleId(editScheduleId)
                    }
                } catch (e: CancellationException) {
                    // Ignore
                } catch (e: CourseAdapterException) {
                    reportError(e)
                } catch (e: SocketTimeoutException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e))
                } catch (e: SocketException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e))
                } catch (e: ConnectException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e))
                } catch (e: UnknownHostException) {
                    reportError(CourseAdapterException.Error.CONNECTION_ERROR.make(e))
                } catch (e: Exception) {
                    reportError(CourseAdapterException.Error.UNKNOWN_ERROR.make(e))
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

    private fun reportError(e: CourseAdapterException) {
        onFinish?.invoke(ImportResult.FAILED, null)
        onError?.invoke(e)
    }

    enum class ImportResult {
        SUCCESS,
        FAILED,
        SUCCESS_WITH_IGNORABLE_CONFLICTS
    }
}