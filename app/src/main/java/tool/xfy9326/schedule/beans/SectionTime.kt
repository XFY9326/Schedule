package tool.xfy9326.schedule.beans

import android.os.Parcelable
import androidx.annotation.IntRange
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.endTimeStr
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr
import tool.xfy9326.schedule.db.DBConst

@Parcelize
data class SectionTime(
    @ColumnInfo(name = DBConst.COLUMN_WEEK_DAY)
    var weekDay: WeekDay,
    @IntRange(from = 1)
    @ColumnInfo(name = DBConst.COLUMN_SECTION_START)
    var start: Int,
    @IntRange(from = 1)
    @ColumnInfo(name = DBConst.COLUMN_SECTION_DURATION)
    var duration: Int,
) : Parcelable {

    companion object {
        private const val SECTION_PERIOD_SYMBOL = "-"
        private const val TIME_DIVIDE = "~"

        val SectionTime.end: Int
            get() = start + duration - 1

        infix fun SectionTime.intersect(sectionTime: SectionTime): Boolean =
            this == sectionTime || (weekDay == sectionTime.weekDay && start <= sectionTime.end && end >= sectionTime.start)

        val SectionTime.description
            get() = if (duration > 1) {
                "$start$SECTION_PERIOD_SYMBOL$end"
            } else {
                start.toString()
            }

        fun SectionTime.scheduleTimeDescription(scheduleTimes: List<ScheduleTime>) =
            "${scheduleTimes[start - 1].startTimeStr}$TIME_DIVIDE${scheduleTimes[end - 1].endTimeStr}"
    }

    operator fun compareTo(sectionTime: SectionTime): Int {
        if (this === sectionTime) return 0
        if (weekDay != sectionTime.weekDay) {
            return weekDay.compareTo(sectionTime.weekDay)
        }
        if (start != sectionTime.start) {
            return start.compareTo(sectionTime.start)
        }
        if (duration != sectionTime.duration) {
            return duration.compareTo(sectionTime.duration)
        }
        return 0
    }
}