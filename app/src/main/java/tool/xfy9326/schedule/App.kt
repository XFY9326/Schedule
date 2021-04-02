package tool.xfy9326.schedule

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.tools.ExceptionHandler

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        ExceptionHandler.init()

        runBlocking {
            val nightMode = AppSettingsDataStore.nightModeTypeFlow.first()
            AppCompatDelegate.setDefaultNightMode(nightMode.modeInt)
        }
    }
}