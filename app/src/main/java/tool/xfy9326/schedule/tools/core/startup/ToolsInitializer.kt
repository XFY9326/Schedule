package tool.xfy9326.schedule.tools.core.startup

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer

@Keep
class ToolsInitializer : Initializer<Unit> {
    companion object {
        private var startUpApplicationContext: Context? = null
        val applicationContext: Context
            get() = startUpApplicationContext ?: error("Application context doesn't initialize yet!")
    }

    override fun create(context: Context) {
        startUpApplicationContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}