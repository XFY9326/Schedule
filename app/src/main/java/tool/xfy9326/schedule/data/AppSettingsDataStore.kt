package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.kt.tryEnumValueOf

object AppSettingsDataStore : AbstractDataStore("Settings") {
    val nightModeType by preferencesKey<String>()
    private val exitAppDirectly by preferencesKey<Boolean>()

    val nightModeTypeFlow = read {
        tryEnumValueOf(it[nightModeType]) ?: NightMode.FOLLOW_SYSTEM
    }

    val exitAppDirectlyFlow = read {
        it[exitAppDirectly] ?: false
    }
}