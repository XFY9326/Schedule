package tool.xfy9326.schedule.data

import io.github.xfy9326.atools.datastore.preference.booleanPrefKey
import io.github.xfy9326.atools.datastore.preference.intPrefKey
import io.github.xfy9326.atools.datastore.preference.stringPrefKey
import io.github.xfy9326.atools.datastore.preference.stringSetPrefKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.beans.CalendarEventDescription
import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.base.AbstractDataStore

object AppSettingsDataStore : AbstractDataStore("Settings") {
    val nightModeType by stringPrefKey()
    private val exitAppDirectly by booleanPrefKey()
    private val keepWebProviderCache by booleanPrefKey()
    private val debugLogsMaxStoreAmount by intPrefKey()
    private val handleException by booleanPrefKey()
    private val calendarSyncScheduleDefault by booleanPrefKey()
    private val calendarSyncScheduleEditableDefault by booleanPrefKey()
    private val calendarSyncScheduleDefaultVisibleDefault by booleanPrefKey()
    private val calendarSyncAddReminderDefault by booleanPrefKey()
    private val calendarSyncReminderMinutes by intPrefKey()
    private val calendarEventDescriptions by stringSetPrefKey()
    private val customActivityTransitionAnimation by booleanPrefKey()
    private val useBrowserDownloadUpgradeFile by booleanPrefKey()
    private val allowImportEmptySchedule by booleanPrefKey()
    private val allowImportIncompleteSchedule by booleanPrefKey()
    private val drawWaterMarkOnScheduleImage by booleanPrefKey()
    private val enableWebCourseProviderConsoleDebug by booleanPrefKey()
    private val autoSwitchToNewImportSchedule by booleanPrefKey()
    val jsCourseImportEnableNetwork by booleanPrefKey()
    val enableOnlineCourseImport by booleanPrefKey()

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

    val exitAppDirectlyFlow = exitAppDirectly.readAsFlow(false)

    val debugLogsMaxStoreAmountFlow = debugLogsMaxStoreAmount.readAsFlow(5)

    val enableWebCourseProviderConsoleDebugFlow = enableWebCourseProviderConsoleDebug.readAsFlow(false)

    val jsCourseImportEnableNetworkFlow = jsCourseImportEnableNetwork.readAsFlow(false)
}