package tool.xfy9326.schedule.utils.schedule

import kotlinx.coroutines.flow.first
import tool.xfy9326.schedule.beans.NextCourse
import tool.xfy9326.schedule.beans.NextCourseInfo
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.compare
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

object NextCourseUtils {
    private val EMPTY_NEXT_COURSE = NextCourse(
        isVacation = false,
        noNextCourse = false,
        nextCourseInfo = null,
        nextAutoRefreshTimeMills = -1
    )

    private fun getCurrentOrNextTimeIndex(times: List<ScheduleTime>, date: Date): Int {
        if (times.isNotEmpty()) {
            val cal = CalendarUtils.getCalendar(date)
            val hour = cal[Calendar.HOUR_OF_DAY]
            val minute = cal[Calendar.MINUTE]
            for ((i, time) in times.withIndex()) {
                if (time.compare(hour, minute) <= 0) {
                    return i
                }
            }
        }
        return -1
    }

    private fun getRefreshTomorrowNextCourse(date: Date) =
        NextCourse(
            isVacation = false,
            noNextCourse = true,
            nextCourseInfo = null,
            nextAutoRefreshTimeMills = CalendarUtils.getTomorrowStartTime(date)
        )

    suspend fun getNextCourseByDate(schedule: Schedule, date: Date = Date()): NextCourse {
        if (date.time > 0 && schedule.times.isEmpty()) {
            return EMPTY_NEXT_COURSE
        }

        val currentClassNumber = getCurrentOrNextTimeIndex(schedule.times, date) + 1
        if (currentClassNumber <= 0) {
            return getRefreshTomorrowNextCourse(date)
        }

        val weekNum = CourseTimeUtils.getWeekNum(schedule, date)
        if (weekNum <= 0) {
            val startTime = schedule.startDate.time
            val currentTime = date.time
            return NextCourse(
                isVacation = true,
                noNextCourse = false,
                nextCourseInfo = null,
                nextAutoRefreshTimeMills = if (currentTime < startTime) startTime else -1
            )
        }

        val weekDay = CalendarUtils.getWeekDay(date)

        val resultPair = ScheduleDBProvider.db.scheduleDAO.getNextScheduleCourseTimeByDate(schedule.scheduleId, weekNum, weekDay, currentClassNumber)
            ?: return getRefreshTomorrowNextCourse(date)

        return NextCourse(
            isVacation = false,
            noNextCourse = false,
            nextCourseInfo = NextCourseInfo(schedule, resultPair.first, resultPair.second),
            nextAutoRefreshTimeMills = CourseTimeUtils.getCourseSectionEndTime(date, schedule.times, resultPair.second.sectionTime)
        )
    }

    suspend fun getCurrentScheduleNextCourse(date: Date = Date()) =
        if (ScheduleUtils.hasInitData()) {
            getNextCourseByDate(ScheduleDataProcessor.currentScheduleFlow.first(), date)
        } else {
            EMPTY_NEXT_COURSE
        }
}