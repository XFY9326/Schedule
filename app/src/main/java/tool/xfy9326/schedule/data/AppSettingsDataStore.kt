package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.kt.tryEnumValueOf

object AppSettingsDataStore : AbstractDataStore("Settings") {
    val nightModeType by preferencesKey<String>()
    private val saveImageWhileSharing by preferencesKey<Boolean>()
    private val exitAppDirectly by preferencesKey<Boolean>()
    val keepWebProviderCache by preferencesKey<Boolean>()
    private val debugLogsMaxStoreAmount by preferencesKey<Int>()
    private val handleException by preferencesKey<Boolean>()

    suspend fun setNightModeType(nightMode: NightMode) = edit {
        it[nightModeType] = nightMode.name
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