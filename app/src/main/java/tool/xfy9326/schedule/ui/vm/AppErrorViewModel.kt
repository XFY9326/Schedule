package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import lib.xfy9326.livedata.setEvent
import tool.xfy9326.schedule.io.CrashFileManager
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class AppErrorViewModel : AbstractViewModel() {
    val crashLog = MutableEventLiveData<String?>()

    fun loadCrashLogDetail(crashLogFileName: String?) {
        if (crashLogFileName == null) {
            crashLog.setEvent(null)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                crashLog.postEvent(CrashFileManager.readCrashLog(crashLogFileName))
            }
        }
    }
}