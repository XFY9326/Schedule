package tool.xfy9326.schedule.ui.activity.base

import androidx.annotation.MainThread
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference

abstract class AbstractActivityModule<T : AbstractActivity>(activity: T) : CoroutineScope by activity.lifecycleScope {
    private var hasInit = false
    private val weakActivity = WeakReference(activity)
    protected val activity: T?
        get() = weakActivity.get()
    protected val isFirstLaunch: Boolean
        get() = requireActivity().isFirstLaunch

    protected fun requireActivity() = weakActivity.get() ?: error("Activity of this module has been gc or not init!")

    @MainThread
    fun init() {
        if (!hasInit) {
            hasInit = true
            onInit()
        } else {
            error("Every module can only init once!")
        }
    }

    protected open fun onInit() {}
}