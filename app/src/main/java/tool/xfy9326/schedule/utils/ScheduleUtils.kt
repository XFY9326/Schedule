package tool.xfy9326.schedule.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import lib.xfy9326.io.IOManager
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.EditError
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.combine
import tool.xfy9326.schedule.kt.intersect
import tool.xfy9326.schedule.kt.iterateAll
import java.util.*

object ScheduleUtils {
    val currentScheduleFlow =
        AppDataStore.currentScheduleIdFlow.combine {
            ScheduleDBProvider.db.scheduleDAO.getSchedule(it).filterNotNull()
        }.shareIn(GlobalScope, SharingStarted.Eagerly, 1)

    private val DEFAULT_SCHEDULE_TIMES by lazy {
        listOf(
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
    }

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

    fun validateScheduleTime(times: List<ScheduleTime>): Boolean {
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

    suspend fun createDefaultSchedule() = getDefaultTermDate().let {
        Schedule(IOManager.resources.getString(R.string.default_schedule_name),
            it.first,
            it.second,
            DEFAULT_SCHEDULE_TIMES,
            ScheduleDataStore.defaultFirstDayOfWeekFlow.first())
    }

    suspend fun createNewSchedule() = getDefaultTermDate().let {
        Schedule(IOManager.resources.getString(R.string.new_schedule_name),
            it.first,
            it.second,
            DEFAULT_SCHEDULE_TIMES,
            ScheduleDataStore.defaultFirstDayOfWeekFlow.first())
    }

    suspend fun saveCurrentSchedule(scheduleTimes: List<ScheduleTime>, courses: List<Course>) {
        val schedule = currentScheduleFlow.first().also {
            it.times = scheduleTimes
            adjustScheduleDateByCourses(it, courses)
        }
        ScheduleDBProvider.db.scheduleDAO.updateScheduleCourses(schedule, courses)
    }

    suspend fun saveNewSchedule(newScheduleName: String?, scheduleTimes: List<ScheduleTime>, courses: List<Course>) {
        val schedule = createNewSchedule().also {
            it.times = scheduleTimes
            adjustScheduleDateByCourses(it, courses)
        }
        if (newScheduleName != null) {
            schedule.name = newScheduleName
        }
        ScheduleDBProvider.db.scheduleDAO.putNewScheduleCourses(schedule, courses)
    }

    suspend fun saveNewSchedule(schedule: Schedule, courses: List<Course>) {
        adjustScheduleDateByCourses(schedule, courses)
        ScheduleDBProvider.db.scheduleDAO.putNewScheduleCourses(schedule, courses)
    }

    private fun adjustScheduleDateByCourses(schedule: Schedule, courses: List<Course>) {
        val maxWeekNum = CourseUtils.getMaxWeekNum(courses)
        val scheduleMaxWeekNum = CourseTimeUtils.getMaxWeekNum(schedule.startDate, schedule.endDate, schedule.weekStart)

        schedule.apply {
            if (maxWeekNum > scheduleMaxWeekNum) {
                endDate = CourseTimeUtils.getTermEndDate(startDate, schedule.weekStart, maxWeekNum)
            }
        }
    }

    fun validateSchedule(schedule: Schedule, scheduleCourses: List<Course>): EditError? {
        if (schedule.name.isBlank() || schedule.name.isEmpty()) {
            return EditError.Type.SCHEDULE_NAME_EMPTY.make()
        }

        val maxWeekNum = CourseTimeUtils.getMaxWeekNum(schedule.startDate, schedule.endDate, schedule.weekStart)

        if (schedule.startDate >= schedule.endDate) {
            return EditError.Type.SCHEDULE_DATE_ERROR.make()
        }
        if (maxWeekNum <= 0) {
            return EditError.Type.SCHEDULE_MAX_WEEK_NUM_ERROR.make()
        }

        for (i1 in schedule.times.indices) {
            val time1 = schedule.times[i1]

            if (time1.startHour >= time1.endHour && time1.startMinute >= time1.endMinute) {
                return EditError.Type.SCHEDULE_TIME_START_END_ERROR.make(i1 + 1)
            }

            for (i2 in (i1 + 1)..schedule.times.lastIndex) {
                val time2 = schedule.times[i2]
                if (time1 intersect time2) {
                    return EditError.Type.SCHEDULE_TIME_CONFLICT_ERROR.make(i1 + 1, i2 + 1)
                }
                if (time1.endHour >= time2.startHour && time1.endMinute >= time2.startMinute) {
                    return EditError.Type.SCHEDULE_TIME_NOT_IN_ONE_DAY_ERROR.make()
                }
            }
        }

        scheduleCourses.iterateAll { course, courseTime ->
            if (courseTime.classTime.classEndTime > schedule.times.size) {
                return EditError.Type.SCHEDULE_COURSE_NUM_ERROR.make(course.name)
            }
            if (courseTime.weekNum.size > maxWeekNum) {
                return EditError.Type.SCHEDULE_COURSE_WEEK_NUM_ERROR.make(course.name)
            }
        }
        return null
    }
}