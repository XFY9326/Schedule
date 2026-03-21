package tool.xfy9326.schedule.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.beans.CalendarEventDescription
import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.tools.datastore.preference.key.booleanKey
import tool.xfy9326.schedule.tools.datastore.preference.key.intKey
import tool.xfy9326.schedule.tools.datastore.preference.key.stringKey
import tool.xfy9326.schedule.tools.datastore.preference.key.stringSetKey

object AppSettingsDataStore : AbstractDataStore("Settings") {
    val nightModeType by stringKey()
    private val keepWebProviderCache by booleanKey()
    private val debugLogsMaxStoreAmount by intKey()
    private val handleException by booleanKey()
    private val calendarSyncScheduleDefault by booleanKey()
    private val calendarSyncScheduleEditableDefault by booleanKey()
    private val calendarSyncScheduleDefaultVisibleDefault by booleanKey()
    private val calendarSyncAddReminderDefault by booleanKey()
    private val calendarSyncReminderMinutes by intKey()
    private val calendarEventDescriptions by stringSetKey()
    private val customActivityTransitionAnimation by booleanKey()
    private val useBrowserDownloadUpgradeFile by booleanKey()
    private val allowImportEmptySchedule by booleanKey()
    private val allowImportIncompleteSchedule by booleanKey()
    private val drawWaterMarkOnScheduleImage by booleanKey()
    private val enableWebCourseProviderConsoleDebug by booleanKey()
    private val autoSwitchToNewImportSchedule by booleanKey()
    val jsCourseImportEnableNetwork by booleanKey()
    val enableOnlineCourseImport by booleanKey()

    suspend fun setNightModeType(nightMode: NightMode) = nightModeType.saveData(nightMode.name)

    suspend fun setEnableOnlineCourseImportFlow(data: Boolean) = enableOnlineCourseImport.saveData(data)

    fun getDefaultScheduleSyncFlow(scheduleId: Long) = read {
        ScheduleSync(
            scheduleId,
            it[calendarSyncScheduleDefault] ?: true,
            it[calendarSyncScheduleDefaultVisibleDefault] ?: true,
            it[calendarSyncScheduleEditableDefault] ?: false,
            it[calendarSyncAddReminderDefault] ?: true
        )
    }

    val autoSwitchToNewImportScheduleFlow = autoSwitchToNewImportSchedule.readAsFlow(false)

    val allowImportIncompleteScheduleFlow = allowImportIncompleteSchedule.readAsFlow(false)

    val drawWaterMarkOnScheduleImageFlow = drawWaterMarkOnScheduleImage.readAsFlow(true)

    val enableOnlineCourseImportFlow = enableOnlineCourseImport.readAsFlow(true)

    val allowImportEmptyScheduleFlow = allowImportEmptySchedule.readAsFlow(false)

    val useCustomActivityTransitionAnimation by lazy {
        runBlocking { customActivityTransitionAnimation.readAsFlow(false).first() }
    }

    val useBrowserDownloadUpgradeFileFlow = useBrowserDownloadUpgradeFile.readAsFlow(false)

    val calendarSyncReminderMinutesFlow = calendarSyncReminderMinutes.readAsFlow(10)

    val calendarEventDescriptionsFlow = calendarEventDescriptions.readEnumSetAsFlow(setOf(CalendarEventDescription.TEACHER))

    val handleExceptionFlow = handleException.readAsFlow(true)

    val keepWebProviderCacheFlow = keepWebProviderCache.readAsFlow(false)

    val nightModeTypeFlow = nightModeType.readEnumAsFlow(NightMode.FOLLOW_SYSTEM)

    val debugLogsMaxStoreAmountFlow = debugLogsMaxStoreAmount.readAsFlow(5)

    val enableWebCourseProviderConsoleDebugFlow = enableWebCourseProviderConsoleDebug.readAsFlow(false)

    val jsCourseImportEnableNetworkFlow = jsCourseImportEnableNetwork.readAsFlow(false)
}