package tool.xfy9326.schedule.beans

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
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
    @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_TIMES)
    var times: List<ScheduleTime>,
    @ColorInt
    var color: Int,
    var weekStart: WeekDay,
) {

    @Ignore
    constructor(
        name: String,
        startDate: Date,
        endDate: Date,
        times: List<ScheduleTime>,
        weekStart: WeekDay,
        color: Int = MaterialColorHelper.random(),
    ) : this(DBConst.DEFAULT_ID, name, startDate, endDate, times, color, weekStart)

    data class Min(
        @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_ID)
        val scheduleId: Long,
        val name: String,
    ) {
        override fun toString() = name
    }

    @Parcelize
    data class Times(
        @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_TIMES)
        val times: List<ScheduleTime>,
    ) : Parcelable
}