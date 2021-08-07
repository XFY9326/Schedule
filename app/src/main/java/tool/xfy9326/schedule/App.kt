@file:Suppress("unused")

package tool.xfy9326.schedule

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import lib.xfy9326.android.kit.initializeToolKit
import tool.xfy9326.schedule.beans.NightMode.Companion.modeInt
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.tools.ExceptionHandler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeToolKit()

        ExceptionHandler.init()

        runBlocking {
            val nightMode = AppSettingsDataStore.nightModeTypeFlow.first()
            AppCompatDelegate.setDefaultNightMode(nightMode.modeInt)
        }
    }
}