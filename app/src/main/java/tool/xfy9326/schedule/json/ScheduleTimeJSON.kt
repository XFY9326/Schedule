package tool.xfy9326.schedule.json

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.endTimeStr
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr

@Serializable
data class ScheduleTimeJSON(
    // HH:mm
    val start: String,
    // HH:mm
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
        ScheduleTime.fromTimeStr(start.trim(), end.trim())
}
