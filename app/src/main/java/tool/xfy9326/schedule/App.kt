package tool.xfy9326.schedule

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.tools.ExceptionHandler
import kotlin.coroutines.EmptyCoroutineContext

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
        val scope = CoroutineScope(EmptyCoroutineContext + SupervisorJob())
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