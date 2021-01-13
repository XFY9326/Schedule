package tool.xfy9326.schedule.json.beans

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.ScheduleTime

@Serializable
data class ScheduleTimeJson(
    val start: String,
    val end: String,
) {
    companion object {
        fun fromScheduleTime(scheduleTime: ScheduleTime) =
            ScheduleTimeJson(scheduleTime.startTimeStr, scheduleTime.endTimeStr)
    }

    fun toScheduleTime() =
        ScheduleTime.fromTimeStr(start, end)
}
