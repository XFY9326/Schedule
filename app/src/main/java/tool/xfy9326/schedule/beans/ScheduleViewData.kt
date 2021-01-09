package tool.xfy9326.schedule.beans

import tool.xfy9326.schedule.utils.CourseManager
import java.util.*

data class ScheduleViewData(
    val scheduleId: Long,
    val startDate: Date,
    val endDate: Date,
    val weekNum: Int,
    val times: Array<ScheduleTime>,
    val cells: Array<CourseCell>,
    val styles: ScheduleStyles,
) {
    val hasWeekendCourse
        get() = CourseManager.hasWeekendCourse(cells)

    constructor(weekNum: Int, schedule: Schedule, cells: Array<CourseCell>, styles: ScheduleStyles) :
            this(schedule.scheduleId, schedule.startDate, schedule.endDate, weekNum, schedule.times, cells, styles)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScheduleViewData) return false

        if (scheduleId != other.scheduleId) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (weekNum != other.weekNum) return false
        if (!times.contentEquals(other.times)) return false
        if (!cells.contentEquals(other.cells)) return false
        if (styles != other.styles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scheduleId.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + weekNum
        result = 31 * result + times.contentHashCode()
        result = 31 * result + cells.contentHashCode()
        result = 31 * result + styles.hashCode()
        return result
    }
}