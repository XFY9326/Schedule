package tool.xfy9326.schedule.ui.vm.base

import androidx.lifecycle.ViewModel

abstract class AbstractViewModel : ViewModel() {
    open fun onViewInitialized(firstInitialized: Boolean) {}
}