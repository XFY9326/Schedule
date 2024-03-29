package tool.xfy9326.schedule.utils.schedule

import io.github.xfy9326.atools.io.IOManager
import kotlinx.coroutines.flow.first
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.Course.Companion.iterateAll
import tool.xfy9326.schedule.beans.EditError
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.intersect
import tool.xfy9326.schedule.beans.SectionTime.Companion.end
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.Calendar
import java.util.Date

object ScheduleUtils {
    const val MAX_WEEK_NUM = 5 * 12 * 4 // 5 years
    private val DEFAULT_SCHEDULE_TIMES by lazy {
        ScheduleTime.listOf(
            8, 0, 8, 45,
            8, 50, 9, 35,
            9, 50, 10, 35,
            10, 40, 11, 25,
            11, 30, 12, 15,
            13, 30, 14, 15,
            14, 20, 15, 5,
            15, 20, 16, 5,
            16, 10, 16, 55,
            17, 5, 17, 50,
            17, 55, 18, 40,
            19, 20, 20, 5,
            20, 10, 20, 55,
            21, 0, 21, 45
        )
    }

    suspend fun hasInitData() =
        AppDataStore.hasCurrentScheduleId() && ScheduleDBProvider.db.scheduleDAO.getScheduleCount() > 0

    fun getDefaultTermDate(weekStart: WeekDay): Pair<Date, Date> {
        CalendarUtils.getCalendar(clearToDate = true).apply {
            val startDate: Date
            val endDate: Date

            var currentYear = get(Calendar.YEAR)
            val currentMonth = get(Calendar.MONTH)
            if (currentMonth > Calendar.JANUARY && currentMonth < Calendar.AUGUST) { // 2.1 - 7.31
                set(Calendar.MONTH, Calendar.MARCH) // 3.1
                set(Calendar.DATE, 1)
                startDate = time

                set(Calendar.MONTH, Calendar.JUNE) // 6.30
                set(Calendar.DATE, 30)
                endDate = time
            } else {
                if (currentMonth <= Calendar.JANUARY) {
                    currentYear--
                }
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, Calendar.SEPTEMBER) // 9.1
                set(Calendar.DATE, 1)
                startDate = time

                set(Calendar.YEAR, ++currentYear)
                set(Calendar.MONTH, Calendar.FEBRUARY) // 2.28
                set(Calendar.DATE, if (currentYear % 4 == 0) 29 else 28)
                endDate = time
            }

            return startDate to CalendarUtils.getLastDateInThisWeek(endDate, weekStart)
        }
    }

    fun validateScheduleTime(times: List<ScheduleTime>): EditError? {
        for (i1 in times.indices) {
            val time1 = times[i1]

            if (time1.startHour > time1.endHour || time1.startHour == time1.endHour && time1.startMinute > time1.endMinute) {
                return EditError.Type.SCHEDULE_TIME_START_END_ERROR.make(i1 + 1)
            }

            for (i2 in (i1 + 1)..times.lastIndex) {
                val time2 = times[i2]
                if (time1 intersect time2) {
                    return EditError.Type.SCHEDULE_TIME_CONFLICT_ERROR.make(i1 + 1, i2 + 1)
                }
                if (time1.endHour > time2.startHour || time1.endHour == time2.startHour && time1.endMinute > time2.startMinute) {
                    return EditError.Type.SCHEDULE_TIME_NOT_IN_ONE_DAY_ERROR.make()
                }
            }
        }
        return null
    }

    suspend fun createDefaultSchedule(): Schedule {
        val weekStart = ScheduleDataStore.defaultFirstDayOfWeekFlow.first()
        val term = getDefaultTermDate(weekStart)
        return Schedule(
            IOManager.resources.getString(R.string.default_schedule_name),
            term.first,
            term.second,
            DEFAULT_SCHEDULE_TIMES,
            weekStart
        )
    }

    suspend fun createNewSchedule(term: Pair<Date, Date>? = null): Schedule {
        val weekStart = ScheduleDataStore.defaultFirstDayOfWeekFlow.first()
        val newTerm = term ?: getDefaultTermDate(weekStart)
        return Schedule(
            IOManager.resources.getString(R.string.new_schedule_name),
            newTerm.first,
            newTerm.second,
            DEFAULT_SCHEDULE_TIMES,
            weekStart
        )
    }

    suspend fun saveCurrentSchedule(scheduleTimes: List<ScheduleTime>, courses: List<Course>): Long {
        val schedule = ScheduleDataProcessor.currentScheduleFlow.first().also {
            it.times = scheduleTimes
            adjustScheduleDateByCourses(it, courses)
        }
        return ScheduleDBProvider.db.scheduleDAO.updateScheduleCourses(schedule, courses)
    }

    suspend fun saveNewSchedule(
        newScheduleName: String?,
        scheduleTimes: List<ScheduleTime>,
        courses: List<Course>,
        term: Pair<Date, Date>? = null
    ): Long {
        val schedule = createNewSchedule(term).also {
            it.times = scheduleTimes
            adjustScheduleDateByCourses(it, courses)
        }
        if (newScheduleName != null) {
            schedule.name = newScheduleName
        }
        return ScheduleDBProvider.db.scheduleDAO.putNewScheduleCourses(schedule, courses)
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
        if (schedule.startDate > schedule.endDate) {
            return EditError.Type.SCHEDULE_DATE_ERROR.make()
        }
        if (CourseTimeUtils.getMaxTermEndDate(schedule.startDate, schedule.weekStart) < schedule.endDate) {
            return EditError.Type.SCHEDULE_TERM_TOO_LONG_ERROR.make()
        }

        val maxWeekNum = CourseTimeUtils.getMaxWeekNum(schedule.startDate, schedule.endDate, schedule.weekStart)
        if (maxWeekNum <= 0) {
            return EditError.Type.SCHEDULE_MAX_WEEK_NUM_ERROR.make()
        }

        validateScheduleTime(schedule.times)?.let { return it }

        scheduleCourses.iterateAll { course, courseTime ->
            if (courseTime.sectionTime.end > schedule.times.size) {
                return EditError.Type.SCHEDULE_COURSE_NUM_ERROR.make(course.name)
            }
            if (courseTime.weekNumArray.size > maxWeekNum) {
                return EditError.Type.SCHEDULE_COURSE_WEEK_NUM_ERROR.make(course.name)
            }
        }
        return null
    }
}