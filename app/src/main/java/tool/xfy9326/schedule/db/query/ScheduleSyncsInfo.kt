package tool.xfy9326.schedule.db.query

import androidx.room.Embedded
import androidx.room.Relation
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.db.DBConst

class ScheduleSyncsInfo(
    @Embedded
    val scheduleMin: Schedule.Min,
    @Relation(
        parentColumn = DBConst.COLUMN_SCHEDULE_ID,
        entityColumn = DBConst.COLUMN_SCHEDULE_ID
    )
    val syncInfo: ScheduleSync?,
)