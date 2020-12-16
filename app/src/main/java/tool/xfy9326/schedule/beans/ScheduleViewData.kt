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
) {
    val hasWeekendCourse
        get() = CourseManager.hasWeekendCourse(cells)

    constructor(weekNum: Int, schedule: Schedule, cells: Array<CourseCell>) :
            this(schedule.scheduleId, schedule.startDate, schedule.endDate, weekNum, schedule.times, cells)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScheduleViewData

        if (scheduleId != other.scheduleId) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (weekNum != other.weekNum) return false
        if (!times.contentEquals(other.times)) return false
        if (!cells.contentEquals(other.cells)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scheduleId.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + weekNum
        result = 31 * result + times.contentHashCode()
        result = 31 * result + cells.contentHashCode()
        return result
    }
}