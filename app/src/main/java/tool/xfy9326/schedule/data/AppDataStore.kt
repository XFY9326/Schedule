package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider

object AppDataStore : AbstractDataStore("App") {
    private val currentScheduleId by longPreferencesKey()
    private val acceptEULA by booleanPreferencesKey()
    private val showFeedbackAttention by booleanPreferencesKey()
    private val ignoreUpdateVersionCode by intPreferencesKey()
    private val readOnlineImportAttention by booleanPreferencesKey()

    val currentScheduleIdFlow = currentScheduleId.readAndInitAsFlow {
        ScheduleDBProvider.db.scheduleDAO.tryInitDefaultSchedule()
    }

    val acceptEULAFlow = acceptEULA.readAsFlow(false)

    val ignoreUpdateVersionCodeFlow = ignoreUpdateVersionCode.readAsFlow(0)

    val readOnlineImportAttentionFlow = readOnlineImportAttention.readAsFlow(false)

    suspend fun setReadOnlineImportAttention(data: Boolean) = readOnlineImportAttention.saveData(data)

    suspend fun hasShownFeedbackAttention() = showFeedbackAttention.readAsShowOnce()

    suspend fun setAcceptEULA(data: Boolean) = acceptEULA.saveData(data)

    suspend fun setIgnoreUpdateVersionCode(data: Int) = ignoreUpdateVersionCode.saveData(data)

    suspend fun setCurrentScheduleId(data: Long) = currentScheduleId.saveData(data)
}