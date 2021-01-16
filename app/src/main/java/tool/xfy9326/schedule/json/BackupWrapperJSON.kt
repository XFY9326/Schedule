package tool.xfy9326.schedule.json

import kotlinx.serialization.Serializable

@Serializable
class BackupWrapperJSON(
    val name: String = BACKUP_NAME,
    val version: Int = VERSION,
    val data: List<ScheduleJSON>,
) {
    companion object {
        private const val BACKUP_NAME = "PureSchedule"
        private const val VERSION = 1
    }
}