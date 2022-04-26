package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.io.okio.readText
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.postEvent
import io.github.xfy9326.atools.livedata.setEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import java.io.File

class AppErrorViewModel : AbstractViewModel() {
    val crashLog = MutableEventLiveData<String?>()

    fun loadCrashLogDetail(crashLogFilePath: String?) {
        if (crashLogFilePath == null) {
            crashLog.setEvent(null)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                crashLog.postEvent(File(crashLogFilePath).readText())
            }
        }
    }
}