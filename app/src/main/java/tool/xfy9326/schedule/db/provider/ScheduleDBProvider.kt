package tool.xfy9326.schedule.db.provider

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.db.dao.ScheduleDAO
import tool.xfy9326.schedule.db.dao.ScheduleSyncDAO
import tool.xfy9326.schedule.db.utils.DBTypeConverter

object ScheduleDBProvider : AbstractDBProvider<ScheduleDBProvider.ScheduleDB>(ScheduleDB::class) {
    override val name: String = "Schedule.db"
    private const val DB_VERSION = 1

    @TypeConverters(DBTypeConverter::class)
    @Database(
        entities = [Schedule::class, Course::class, CourseTime::class, ScheduleSync::class],
        version = DB_VERSION,
        exportSchema = true
    )
    abstract class ScheduleDB : RoomDatabase() {
        abstract val scheduleDAO: ScheduleDAO

        abstract val scheduleSyncDao: ScheduleSyncDAO
    }
}