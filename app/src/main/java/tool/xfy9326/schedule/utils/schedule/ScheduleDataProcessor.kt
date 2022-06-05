package tool.xfy9326.schedule.utils.schedule

import io.github.xfy9326.atools.coroutines.AppScope
import io.github.xfy9326.atools.coroutines.combine
import io.github.xfy9326.atools.coroutines.combineTransform
import io.github.xfy9326.atools.coroutines.withTryLock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider

object ScheduleDataProcessor {
    private val hasPreloadLock = Mutex()

    private fun <T> Flow<T>.preload() = flowOn(Dispatchers.IO).shareIn(AppScope, SharingStarted.Eagerly, 1)

    suspend fun preload() = withContext(Dispatchers.Default) {
        runCatching {
            hasPreloadLock.withTryLock {
                listOf(
                    async { weekNumInfoFlow.firstOrNull() },
                    async { scheduleViewDataFlow.firstOrNull() },
                    async { scheduleBackgroundFlow.firstOrNull() }
                ).awaitAll()
            }
        }.isSuccess
    }

    val currentScheduleFlow =
        AppDataStore.currentScheduleIdFlow.combine {
            ScheduleDBProvider.db.scheduleDAO.getSchedule(it).filterNotNull()
        }.preload()

    val currentScheduleTimesFlow =
        AppDataStore.currentScheduleIdFlow.combine {
            ScheduleDBProvider.db.scheduleDAO.getScheduleTimes(it).filterNotNull()
        }.preload()

    private val currentScheduleCourseDataFlow = currentScheduleFlow.combineTransform(
        combineTransform = {
            ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(it.scheduleId)
        },
        transform = { schedule, courses ->
            schedule to courses
        }
    ).preload()

    fun addCurrentScheduleCourseDataGlobalListener(listener: suspend (Schedule) -> Unit): Job =
        AppScope.launch(Dispatchers.IO) {
            currentScheduleCourseDataFlow.collect {
                listener(it.first)
            }
        }

    val weekNumInfoFlow = currentScheduleFlow.map {
        CourseTimeUtils.getWeekNum(it) to CourseTimeUtils.getMaxWeekNum(it.startDate, it.endDate, it.weekStart)
    }.preload()

    val weekNumFlow = weekNumInfoFlow.map {
        it.first
    }.preload()

    val scheduleViewDataFlow = currentScheduleCourseDataFlow.combine(ScheduleDataStore.scheduleStylesFlow) { data, styles ->
        ScheduleBuildBundle(data.first, data.second, styles)
    }.preload()

    val scheduleBackgroundFlow = ScheduleDataStore.scheduleBackgroundBuildBundleFlow.preload()
}