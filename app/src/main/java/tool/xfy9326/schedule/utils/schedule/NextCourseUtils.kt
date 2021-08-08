package tool.xfy9326.schedule.utils.schedule

import kotlinx.coroutines.flow.first
import lib.xfy9326.android.kit.ApplicationInstance
import tool.xfy9326.schedule.beans.NextCourse
import tool.xfy9326.schedule.beans.NextCourseInfo
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.compare
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

object NextCourseUtils {
    private const val FIRST_COURSE_MINUTE_OFFSET = 20

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

    private fun getRefreshTomorrowNextCourse(date: Date, schedule: Schedule): NextCourse {
        val scheduleTime = schedule.times.firstOrNull()
        val time = CourseTimeUtils.getOffsetTime(
            date = date,
            hour = scheduleTime?.startHour ?: 0,
            minute = scheduleTime?.startMinute ?: 0,
            dayOffset = 1,
            minuteOffset = if (scheduleTime == null) 0 else FIRST_COURSE_MINUTE_OFFSET
        )
        return NextCourse(
            isVacation = false,
            noNextCourse = true,
            nextCourseInfo = null,
            nextAutoRefreshTime = time
        )
    }

    suspend fun getNextCourseByDate(schedule: Schedule, date: Date = Date()): NextCourse {
        if (schedule.times.isEmpty()) {
            return NextCourse(
                isVacation = false,
                noNextCourse = false,
                nextCourseInfo = null,
                nextAutoRefreshTime = -1
            )
        }

        val currentClassNumber = getCurrentOrNextTimeIndex(schedule.times, date) + 1
        if (currentClassNumber <= 0) {
            return getRefreshTomorrowNextCourse(date, schedule)
        }

        val weekNum = CourseTimeUtils.getWeekNum(schedule, date)
        if (weekNum <= 0) {
            val startTime = schedule.startDate.time
            val currentTime = date.time
            return NextCourse(
                isVacation = true,
                noNextCourse = false,
                nextCourseInfo = null,
                nextAutoRefreshTime = if (currentTime < startTime) startTime else -1
            )
        }

        val weekDay = CalendarUtils.getWeekDay(date)

        val dao = ScheduleDBProvider.db.scheduleDAO
        val courseTime = dao.getNextScheduleCourseTimeByDate(schedule.scheduleId, weekNum, weekDay, currentClassNumber)
            ?: return getRefreshTomorrowNextCourse(date, schedule)
        val course = dao.getSingleCourse(courseTime.courseId) ?: error("Can't get Course by CourseTime.courseId from DB!")

        return NextCourse(
            isVacation = false,
            noNextCourse = false,
            nextCourseInfo = NextCourseInfo(ApplicationInstance, schedule, course, courseTime),
            nextAutoRefreshTime = CourseTimeUtils.getClassEndTime(date, schedule.times, courseTime.classTime)
        )
    }

    suspend fun getCurrentScheduleNextCourse() = getNextCourseByDate(ScheduleDataProcessor.currentScheduleFlow.first())
}