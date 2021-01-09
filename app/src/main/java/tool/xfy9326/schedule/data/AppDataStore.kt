package tool.xfy9326.schedule.data

import kotlinx.coroutines.flow.filterNotNull
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider

object AppDataStore : AbstractDataStore("App") {
    private val currentScheduleId by preferencesKey<Long>()
    private val acceptEULA by preferencesKey<Boolean>()

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

    val acceptEULAFlow = read {
        it[acceptEULA] ?: false
    }

    suspend fun setAcceptEULA(data: Boolean) = edit {
        it[acceptEULA] = data
    }

    suspend fun setCurrentScheduleId(data: Long) = edit {
        it[currentScheduleId] = data
    }
}