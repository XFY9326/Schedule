package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.ScheduleTime

@Serializable
data class ScheduleTimeJSON(
    val start: String,
    val end: String,
) {
    companion object {
        fun fromScheduleTime(scheduleTime: ScheduleTime) =
            ScheduleTimeJSON(scheduleTime.startTimeStr, scheduleTime.endTimeStr)
    }

    fun toScheduleTime() =
        ScheduleTime.fromTimeStr(start, end)
}
