package tool.xfy9326.schedule.beans

import androidx.annotation.IntRange
import androidx.room.ColumnInfo
import java.io.Serializable

data class ClassTime(
    @ColumnInfo(name = DBConst.COLUMN_WEEK_DAY)
    var weekDay: WeekDay,
    @IntRange(from = 1)
    @ColumnInfo(name = DBConst.COLUMN_CLASS_START_TIME)
    var classStartTime: Int,
    @IntRange(from = 1)
    @ColumnInfo(name = DBConst.COLUMN_CLASS_DURATION)
    var classDuration: Int,
) : Serializable {
    val classEndTime
        get() = classStartTime + classDuration - 1

    companion object {
        private const val CLASS_TIME_PERIOD_SYMBOL = "-"
        private const val TIME_DIVIDE = "~"
    }

    operator fun compareTo(classTime: ClassTime): Int {
        if (weekDay != classTime.weekDay) {
            return -weekDay.compareTo(classTime.weekDay)
        }
        if (classStartTime != classTime.classStartTime) {
            return -classStartTime.compareTo(classTime.classStartTime)
        }
        if (classDuration != classTime.classDuration) {
            return -classDuration.compareTo(classTime.classDuration)
        }
        return 0
    }

    fun classTimeDescription(scheduleTimes: Array<ScheduleTime>? = null) =
        if (scheduleTimes == null) {
            if (classDuration > 1) {
                "$classStartTime$CLASS_TIME_PERIOD_SYMBOL$classEndTime"
            } else {
                classStartTime.toString()
            }
        } else {
            "${scheduleTimes[classStartTime - 1].startTimeStr}$TIME_DIVIDE${scheduleTimes[classEndTime - 1].endTimeStr}"
        }
}
