package tool.xfy9326.schedule.json.backup

import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.kt.APP_ID

@Serializable
data class BackupWrapperJSON(
    val name: String = BACKUP_NAME,
    val version: Int = VERSION,
    val data: List<ScheduleJSON>,
) {
    init {
        require(VERSION >= version) { "Incompatible or too high JSON data version!" }
    }

    companion object {
        private const val BACKUP_NAME = APP_ID
        private const val VERSION = 1
    }
}