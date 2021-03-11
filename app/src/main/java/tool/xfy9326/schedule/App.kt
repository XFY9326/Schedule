package tool.xfy9326.schedule

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import lib.xfy9326.io.IOManager
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

        IOManager.init(this)

        ExceptionHandler.init()

        runBlocking {
            val nightMode = AppSettingsDataStore.nightModeTypeFlow.first()
            AppCompatDelegate.setDefaultNightMode(nightMode.modeInt)
        }
    }
}