package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.base.AbstractDataStore

object AppSettingsDataStore : AbstractDataStore("Settings") {
    val nightModeType by preferencesKey<String>()
    private val saveImageWhileSharing by preferencesKey<Boolean>()
    private val exitAppDirectly by preferencesKey<Boolean>()
    val keepWebProviderCache by preferencesKey<Boolean>()
    private val debugLogsMaxStoreAmount by preferencesKey<Int>()
    private val handleException by preferencesKey<Boolean>()
    private val calendarSyncScheduleDefault by preferencesKey<Boolean>()
    private val calendarSyncScheduleEditableDefault by preferencesKey<Boolean>()
    private val calendarSyncScheduleDefaultVisibleDefault by preferencesKey<Boolean>()
    private val calendarSyncAddReminderDefault by preferencesKey<Boolean>()
    private val calendarSyncReminderMinutes by preferencesKey<Int>()

    suspend fun setNightModeType(nightMode: NightMode) = nightModeType.saveData(nightMode.name)

    fun getDefaultScheduleSyncFlow(scheduleId: Long) = read {
        ScheduleSync(
            scheduleId,
            it[calendarSyncScheduleDefault] ?: true,
            it[calendarSyncScheduleDefaultVisibleDefault] ?: true,
            it[calendarSyncScheduleEditableDefault] ?: false,
            it[calendarSyncAddReminderDefault] ?: false
        )
    }

    val calendarSyncReminderMinutesFlow = calendarSyncReminderMinutes.readAsFlow(10)

    val handleExceptionFlow = handleException.readAsFlow(true)

    val keepWebProviderCacheFlow = keepWebProviderCache.readAsFlow(false)

    val nightModeTypeFlow = nightModeType.readEnumAsFlow(NightMode.FOLLOW_SYSTEM)

    val exitAppDirectlyFlow = exitAppDirectly.readAsFlow(false)

    val saveImageWhileSharingFlow = saveImageWhileSharing.readAsFlow(false)

    val debugLogsMaxStoreAmountFlow = debugLogsMaxStoreAmount.readAsFlow(5)
}