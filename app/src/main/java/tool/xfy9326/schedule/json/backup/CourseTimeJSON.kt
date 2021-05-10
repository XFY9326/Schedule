package tool.xfy9326.schedule.json.backup

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.json.serializer.WeekDayShortNameSerializer
import tool.xfy9326.schedule.json.serializer.WeekNumStringSerializer

@Serializable
data class CourseTimeJSON(
    @Serializable(WeekNumStringSerializer::class)
    val weekNum: BooleanArray,
    @Serializable(WeekDayShortNameSerializer::class)
    val weekDay: WeekDay,
    val start: Int,
    val duration: Int,
    val location: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CourseTimeJSON) return false

        if (!weekNum.contentEquals(other.weekNum)) return false
        if (weekDay != other.weekDay) return false
        if (start != other.start) return false
        if (duration != other.duration) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = weekNum.contentHashCode()
        result = 31 * result + weekDay.hashCode()
        result = 31 * result + start
        result = 31 * result + duration
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }
}