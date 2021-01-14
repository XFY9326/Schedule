package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.ScheduleTime

@Serializable
data class ScheduleTimeJSON(
    val start: String,
    val end: String,
) {
    init {
        require(start.isNotEmpty() && end.isNotEmpty()) { "Schedule time empty!" }
    }

    companion object {
        fun fromScheduleTime(scheduleTime: ScheduleTime) =
            ScheduleTimeJSON(scheduleTime.startTimeStr, scheduleTime.endTimeStr)
    }

    fun toScheduleTime() =
        ScheduleTime.fromTimeStr(start, end)
}
