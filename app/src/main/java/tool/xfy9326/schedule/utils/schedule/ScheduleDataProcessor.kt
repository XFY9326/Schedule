package tool.xfy9326.schedule.utils.schedule

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course.Companion.hasEmptyWeekNumCourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.tools.coroutines.combine
import tool.xfy9326.schedule.tools.coroutines.combineTransform

object ScheduleDataProcessor {
    @OptIn(DelicateCoroutinesApi::class)
    private fun <T> Flow<T>.preload() = flowOn(Dispatchers.IO).shareIn(GlobalScope, SharingStarted.Eagerly, 1)

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

    @OptIn(DelicateCoroutinesApi::class)
    fun addCurrentScheduleCourseDataGlobalListener(listener: suspend (Schedule) -> Unit): Job =
        GlobalScope.launch(Dispatchers.IO) {
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

    val scheduleAlertFlow = currentScheduleCourseDataFlow.map {
        it.second.any { c -> c.hasEmptyWeekNumCourseTime() }
    }

    val scheduleViewDataFlow = currentScheduleCourseDataFlow.combine(ScheduleDataStore.scheduleStylesFlow) { data, styles ->
        ScheduleBuildBundle(data.first, data.second, styles)
    }.preload()

    val scheduleBackgroundFlow = ScheduleDataStore.scheduleBackgroundBuildBundleFlow.preload()
}