package tool.xfy9326.schedule.ui.activity.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference

abstract class AbstractActivityModule<T : AppCompatActivity>(activity: T) : CoroutineScope by activity.lifecycleScope {
    private val weakActivity = WeakReference(activity)
    protected val activity: T?
        get() = weakActivity.get()

    protected fun requireActivity() = weakActivity.get()!!

    abstract fun init()
}