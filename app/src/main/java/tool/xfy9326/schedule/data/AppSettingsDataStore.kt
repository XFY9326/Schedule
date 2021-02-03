package tool.xfy9326.schedule.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.base.AbstractDataStore

object AppSettingsDataStore : AbstractDataStore("Settings") {
    val nightModeType by stringPreferencesKey()
    private val saveImageWhileSharing by booleanPreferencesKey()
    private val exitAppDirectly by booleanPreferencesKey()
    private val keepWebProviderCache by booleanPreferencesKey()
    private val debugLogsMaxStoreAmount by intPreferencesKey()
    private val handleException by booleanPreferencesKey()
    private val calendarSyncScheduleDefault by booleanPreferencesKey()
    private val calendarSyncScheduleEditableDefault by booleanPreferencesKey()
    private val calendarSyncScheduleDefaultVisibleDefault by booleanPreferencesKey()
    private val calendarSyncAddReminderDefault by booleanPreferencesKey()
    private val calendarSyncReminderMinutes by intPreferencesKey()
    private val customActivityTransitionAnimation by booleanPreferencesKey()
    private val useBrowserDownloadUpgradeFile by booleanPreferencesKey()
    private val allowImportEmptySchedule by booleanPreferencesKey()
    val enableOnlineCourseImport by booleanPreferencesKey()

    suspend fun setNightModeType(nightMode: NightMode) = nightModeType.saveData(nightMode.name)

    suspend fun setEnableOnlineCourseImportFlow(data: Boolean) = enableOnlineCourseImport.saveData(data)

    fun getDefaultScheduleSyncFlow(scheduleId: Long) = read {
        ScheduleSync(
            scheduleId,
            it[calendarSyncScheduleDefault] ?: true,
            it[calendarSyncScheduleDefaultVisibleDefault] ?: true,
            it[calendarSyncScheduleEditableDefault] ?: false,
            it[calendarSyncAddReminderDefault] ?: false
        )
    }

    val enableOnlineCourseImportFlow = enableOnlineCourseImport.readAsFlow(true)

    val allowImportEmptyScheduleFlow = allowImportEmptySchedule.readAsFlow(false)

    val useCustomActivityTransitionAnimation by lazy {
        runBlocking { customActivityTransitionAnimation.readAsFlow(false).first() }
    }

    val useBrowserDownloadUpgradeFileFlow = useBrowserDownloadUpgradeFile.readAsFlow(false)

    val calendarSyncReminderMinutesFlow = calendarSyncReminderMinutes.readAsFlow(10)

    val handleExceptionFlow = handleException.readAsFlow(true)

    val keepWebProviderCacheFlow = keepWebProviderCache.readAsFlow(false)

    val nightModeTypeFlow = nightModeType.readEnumAsFlow(NightMode.FOLLOW_SYSTEM)

    val exitAppDirectlyFlow = exitAppDirectly.readAsFlow(false)

    val saveImageWhileSharingFlow = saveImageWhileSharing.readAsFlow(false)

    val debugLogsMaxStoreAmountFlow = debugLogsMaxStoreAmount.readAsFlow(5)
}