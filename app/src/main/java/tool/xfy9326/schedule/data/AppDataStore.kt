package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider

object AppDataStore : AbstractDataStore("App") {
    private val currentScheduleId by preferencesKey<Long>()
    private val acceptEULA by preferencesKey<Boolean>()
    private val showFeedbackAttention by preferencesKey<Boolean>()

    val currentScheduleIdFlow = currentScheduleId.readAndInitAsFlow {
        ScheduleDBProvider.db.scheduleDAO.tryInitDefaultSchedule()
    }

    val acceptEULAFlow = acceptEULA.readAsFlow(false)

    suspend fun hasShownFeedbackAttention() = showFeedbackAttention.readAsShowOnce()

    suspend fun setAcceptEULA(data: Boolean) = acceptEULA.saveData(data)

    suspend fun setCurrentScheduleId(data: Long) = currentScheduleId.saveData(data)
}