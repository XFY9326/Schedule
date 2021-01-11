package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.kt.tryEnumValueOf

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

    suspend fun setNightModeType(nightMode: NightMode) = edit {
        it[nightModeType] = nightMode.name
    }

    fun getDefaultScheduleSyncFlow(scheduleId: Long) = read {
        ScheduleSync(
            scheduleId,
            it[calendarSyncScheduleDefault] ?: true,
            it[calendarSyncScheduleDefaultVisibleDefault] ?: true,
            it[calendarSyncScheduleEditableDefault] ?: false,
            it[calendarSyncAddReminderDefault] ?: false
        )
    }

    val calendarSyncReminderMinutesFlow = read {
        it[calendarSyncReminderMinutes] ?: 10
    }

    val handleExceptionFlow = read {
        it[handleException] ?: true
    }

    val keepWebProviderCacheFlow = read {
        it[keepWebProviderCache] ?: false
    }

    val nightModeTypeFlow = read {
        tryEnumValueOf(it[nightModeType]) ?: NightMode.FOLLOW_SYSTEM
    }

    val exitAppDirectlyFlow = read {
        it[exitAppDirectly] ?: false
    }

    val saveImageWhileSharingFlow = read {
        it[saveImageWhileSharing] ?: false
    }

    val debugLogsMaxStoreAmountFlow = read {
        it[debugLogsMaxStoreAmount] ?: 5
    }
}