package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.tools.MaterialColorHelper
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
    var times: Array<ScheduleTime>,
    @ColorInt
    var color: Int,
    var weekStart: WeekDay,
) {
    constructor(name: String, times: Array<ScheduleTime>, color: Int, weekStart: WeekDay) :
            this(DBConst.DEFAULT_ID, name, Date(), Date(), times, color, weekStart)

    constructor(
        name: String,
        startDate: Date,
        endDate: Date,
        times: Array<ScheduleTime>,
        weekStart: WeekDay,
        color: Int = MaterialColorHelper.random(),
    ) :
            this(DBConst.DEFAULT_ID, name, startDate, endDate, times, color, weekStart)

    data class Min(
        @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_ID)
        val scheduleId: Long,
        val name: String,
    ) {
        override fun toString() = name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Schedule) return false

        if (scheduleId != other.scheduleId) return false
        if (name != other.name) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (!times.contentEquals(other.times)) return false
        if (color != other.color) return false
        if (weekStart != other.weekStart) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scheduleId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + times.contentHashCode()
        result = 31 * result + color
        result = 31 * result + weekStart.hashCode()
        return result
    }
}