package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.io.PathConst
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import java.io.File

class AppErrorViewModel : AbstractViewModel() {
    val crashLog = MutableEventLiveData<String?>()

    fun loadCrashLogDetail(crashLogFileName: String?) = viewModelScope.launch(Dispatchers.IO) {
        if (crashLogFileName == null) {
            crashLog.postEvent(null)
        } else {
            crashLog.postEvent(TextIO.readText(File(PathConst.LogPath, crashLogFileName)))
        }
    }
}