package tool.xfy9326.schedule.data

import kotlinx.coroutines.flow.filterNotNull
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider

object AppDataStore : AbstractDataStore("App") {
    private val currentScheduleId by preferencesKey<Long>()

    val currentScheduleIdFlow = read {
        if (it.contains(currentScheduleId)) {
            it[currentScheduleId]
        } else {
            ScheduleDBProvider.db.scheduleDAO.tryInitDefaultSchedule()?.let { id ->
                setCurrentScheduleId(id)
            }
            null
        }
    }.filterNotNull()

    suspend fun setCurrentScheduleId(data: Long) = edit {
        it[currentScheduleId] = data
    }
}