package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lib.xfy9326.io.processor.textReader
import lib.xfy9326.io.utils.asParentOf
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.PathManager

class AppErrorViewModel : AbstractViewModel() {
    val crashLog = MutableEventLiveData<String?>()

    fun loadCrashLogDetail(crashLogFileName: String?) = viewModelScope.launch {
        if (crashLogFileName == null) {
            crashLog.postEvent(null)
        } else {
            crashLog.postEvent(PathManager.LogDir.asParentOf(crashLogFileName).textReader().read())
        }
    }
}