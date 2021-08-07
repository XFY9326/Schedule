package tool.xfy9326.schedule.utils.schedule

import kotlinx.coroutines.flow.first
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.compare
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

object NextCourseUtils {

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

    private suspend fun getNextCourseByDate(schedule: Schedule, date: Date): Course? {
        val weekNum = CourseTimeUtils.getWeekNum(schedule, date)
        val weekDay = CalendarUtils.getWeekDay(date)
        val currentClassNumber = getCurrentOrNextTimeIndex(schedule.times, date) + 1
        val dao = ScheduleDBProvider.db.scheduleDAO
        val courseTime = dao.getNextScheduleCourseTimeByDate(schedule.scheduleId, weekNum, weekDay, currentClassNumber)
        return courseTime?.let { dao.getSingleCourse(it.courseId) }
    }

    suspend fun getNextCourseByDate(date: Date) =
        getNextCourseByDate(ScheduleUtils.currentScheduleFlow.first(), date)
}