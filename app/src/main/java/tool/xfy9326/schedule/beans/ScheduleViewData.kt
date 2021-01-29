package tool.xfy9326.schedule.beans

import tool.xfy9326.schedule.utils.CourseUtils
import java.util.*

data class ScheduleViewData(
    val scheduleId: Long,
    val startDate: Date,
    val endDate: Date,
    val weekNum: Int,
    val weekStart: WeekDay,
    val times: List<ScheduleTime>,
    val cells: List<CourseCell>,
    val styles: ScheduleStyles,
) {
    val hasWeekendCourse
        get() = CourseUtils.hasWeekendCourse(cells)

    constructor(weekNum: Int, schedule: Schedule, cells: List<CourseCell>, styles: ScheduleStyles) :
            this(schedule.scheduleId, schedule.startDate, schedule.endDate, weekNum, schedule.weekStart, schedule.times, cells, styles)
}