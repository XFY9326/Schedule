package tool.xfy9326.schedule.ui.vm.base

import androidx.lifecycle.ViewModel

abstract class AbstractViewModel : ViewModel() {
    private var hasInitialized = false

    fun initViewModel(firstInitialized: Boolean) {
        if (firstInitialized || !hasInitialized) {
            hasInitialized = true
            onViewInitialized(true)
        } else {
            onViewInitialized(false)
        }
    }

    protected open fun onViewInitialized(firstInitialize: Boolean) {}
}