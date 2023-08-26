package tool.xfy9326.schedule.db.provider

import tool.xfy9326.schedule.db.provider.room.ScheduleDB

object ScheduleDBProvider : AbstractDBProvider<ScheduleDB>(ScheduleDB::class) {
    const val DB_VERSION = 2
    override val name: String = "Schedule.db"
}