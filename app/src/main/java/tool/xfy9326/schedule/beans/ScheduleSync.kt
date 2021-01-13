package tool.xfy9326.schedule.beans

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import tool.xfy9326.schedule.db.DBConst

@Entity(
    tableName = DBConst.TABLE_SCHEDULE_SYNC,
    foreignKeys = [ForeignKey(
        entity = Schedule::class,
        parentColumns = [DBConst.COLUMN_SCHEDULE_ID],
        childColumns = [DBConst.COLUMN_SCHEDULE_ID],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )]
)
data class ScheduleSync(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConst.COLUMN_SYNC_ID)
    var syncId: Long,
    @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_ID, index = true)
    var scheduleId: Long,
    var syncable: Boolean,
    var defaultVisible: Boolean,
    var editable: Boolean,
    var addReminder: Boolean,
) {
    constructor(scheduleId: Long, syncable: Boolean, defaultVisible: Boolean, editable: Boolean, addReminder: Boolean) :
            this(DBConst.DEFAULT_ID, scheduleId, syncable, defaultVisible, editable, addReminder)

}