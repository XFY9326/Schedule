package tool.xfy9326.schedule.utils.view

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import lib.xfy9326.android.kit.ApplicationScope
import lib.xfy9326.kit.combineTransform
import lib.xfy9326.kit.withTryLock
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils

object ScheduleViewDataProcessor {
    private val hasPreloadLock = Mutex()

    private fun <T> Flow<T>.preload() = flowOn(Dispatchers.IO).shareIn(ApplicationScope, SharingStarted.Eagerly, 1)

    suspend fun preload() = withContext(Dispatchers.IO + SupervisorJob()) {
        hasPreloadLock.withTryLock {
            listOf(
                async { weekNumInfoFlow.firstOrNull() },
                async { scheduleBuildDataFlow.firstOrNull() },
                async { scheduleBackgroundFlow.firstOrNull() }
            ).awaitAll()
        }
    }

    val weekNumInfoFlow = ScheduleUtils.currentScheduleFlow.map {
        CourseTimeUtils.getWeekNum(it) to CourseTimeUtils.getMaxWeekNum(it.startDate, it.endDate, it.weekStart)
    }.preload()

    val weekNumFlow = weekNumInfoFlow.map {
        it.first
    }.preload()

    val scheduleBuildDataFlow = ScheduleUtils.currentScheduleFlow.combine(ScheduleDataStore.scheduleStylesFlow) { schedule, styles ->
        schedule to styles
    }.combineTransform(
        combineTransform = {
            ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(it.first.scheduleId)
        },
        transform = { pair, courses ->
            ScheduleBuildBundle(pair.first, courses, pair.second)
        }
    ).preload()

    val scheduleBackgroundFlow = ScheduleDataStore.scheduleBackgroundBuildBundleFlow.preload()
}