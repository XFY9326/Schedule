package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.utils.CourseTimeUtils
import java.util.*

@Entity(
    tableName = DBConst.TABLE_SCHEDULE
)
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_ID)
    var scheduleId: Long,
    var name: String,
    var startDate: Date,
    var endDate: Date,
    var maxWeekNum: Int,
    var times: Array<ScheduleTime>,
    @ColorInt
    var color: Int,
) {
    constructor(name: String, startDate: Date, endDate: Date, times: Array<ScheduleTime>) :
            this(DBConst.DEFAULT_ID, name, startDate, endDate, CourseTimeUtils.getMaxWeekNum(startDate, endDate), times, MaterialColorHelper.random())

    fun refreshMaxWeekNum() {
        maxWeekNum = CourseTimeUtils.getMaxWeekNum(startDate, this.endDate)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Schedule

        if (scheduleId != other.scheduleId) return false
        if (name != other.name) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (!times.contentEquals(other.times)) return false
        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scheduleId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + times.contentHashCode()
        result = 31 * result + color
        return result
    }

    data class Min(
        @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_ID)
        val scheduleId: Long,
        val name: String,
    ) {
        override fun toString() = name
    }
}