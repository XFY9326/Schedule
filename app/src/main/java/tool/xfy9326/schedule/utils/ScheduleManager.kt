package tool.xfy9326.schedule.utils

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.combine
import tool.xfy9326.schedule.kt.intersect
import java.util.*

object ScheduleManager {
    private val DEFAULT_SCHEDULE_TIMES = arrayOf(
        ScheduleTime(8, 0, 8, 45),
        ScheduleTime(8, 50, 9, 35),
        ScheduleTime(9, 50, 10, 35),
        ScheduleTime(10, 40, 11, 25),
        ScheduleTime(11, 30, 12, 15),
        ScheduleTime(13, 30, 14, 15),
        ScheduleTime(14, 20, 15, 5),
        ScheduleTime(15, 20, 16, 5),
        ScheduleTime(16, 10, 16, 55),
        ScheduleTime(17, 5, 17, 50),
        ScheduleTime(17, 55, 18, 40),
        ScheduleTime(19, 20, 20, 5),
        ScheduleTime(20, 10, 20, 55),
        ScheduleTime(21, 0, 21, 45)
    )

    private fun getDefaultTermDate(): Pair<Date, Date> {
        CalendarUtils.getCalendar(clearToDate = true).apply {
            val startDate: Date
            val endDate: Date

            var currentYear = get(Calendar.YEAR)
            val currentMonth = get(Calendar.MONTH)
            if (currentMonth < Calendar.SEPTEMBER && currentMonth > Calendar.JANUARY) {
                set(Calendar.MONTH, Calendar.FEBRUARY)
                set(Calendar.DATE, 1)
                startDate = time

                set(Calendar.MONTH, Calendar.JUNE)
                set(Calendar.DATE, 30)
                endDate = time
            } else {
                if (currentMonth <= Calendar.JANUARY) {
                    currentYear--
                }
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, Calendar.SEPTEMBER)
                set(Calendar.DATE, 1)
                startDate = time

                set(Calendar.YEAR, currentYear + 1)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DATE, 30)
                endDate = time
            }

            return startDate to endDate
        }
    }

    fun validateScheduleTime(times: Array<ScheduleTime>): Boolean {
        for (i1 in times.indices) {
            val time1 = times[i1]

            if (time1.startHour >= time1.endHour && time1.startMinute >= time1.endMinute) {
                return false
            }

            for (i2 in (i1 + 1)..times.lastIndex) {
                val time2 = times[i2]
                if (time1 intersect time2) {
                    return false
                }
                if (time1.endHour >= time2.startHour && time1.endMinute >= time2.startMinute) {
                    return false
                }
            }
        }
        return true
    }

    fun createDefaultSchedule() = getDefaultTermDate().let {
        Schedule(App.instance.getString(R.string.default_schedule_name), it.first, it.second, DEFAULT_SCHEDULE_TIMES)
    }

    fun createNewSchedule() = getDefaultTermDate().let {
        Schedule(App.instance.getString(R.string.new_schedule_name), it.first, it.second, DEFAULT_SCHEDULE_TIMES)
    }

    fun getCurrentScheduleFlow() =
        AppDataStore.currentScheduleIdFlow.combine {
            ScheduleDBProvider.db.scheduleDAO.getSchedule(it).filterNotNull()
        }

    suspend fun saveCurrentSchedule(scheduleTimes: Array<ScheduleTime>, courses: Array<Course>) {
        val schedule = applyNewSettingsToSchedule(getCurrentScheduleFlow().first(), scheduleTimes, courses)
        ScheduleDBProvider.db.scheduleDAO.updateScheduleCourses(schedule, courses)
    }

    suspend fun saveNewSchedule(newScheduleName: String?, scheduleTimes: Array<ScheduleTime>, courses: Array<Course>) {
        val schedule = applyNewSettingsToSchedule(createNewSchedule(), scheduleTimes, courses)
        if (newScheduleName != null) {
            schedule.name = newScheduleName
        }
        ScheduleDBProvider.db.scheduleDAO.putNewScheduleCourses(schedule, courses)
    }

    private suspend fun applyNewSettingsToSchedule(schedule: Schedule, scheduleTimes: Array<ScheduleTime>, courses: Array<Course>): Schedule {
        val firstDayOfWeek = ScheduleDataStore.firstDayOfWeekFlow.first()
        val maxWeekNum = CourseManager.getMaxWeekNum(courses)
        val scheduleMaxWeekNum = CourseTimeUtils.getMaxWeekNum(schedule.startDate, schedule.endDate, firstDayOfWeek)

        return schedule.apply {
            times = scheduleTimes
            if (maxWeekNum > scheduleMaxWeekNum) {
                endDate = CourseTimeUtils.getTermEndDate(startDate, firstDayOfWeek, maxWeekNum)
            }
        }
    }
}