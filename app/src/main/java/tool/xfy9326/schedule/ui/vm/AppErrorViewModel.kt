package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.tools.io.okio.readText
import tool.xfy9326.schedule.tools.livedata.MutableEventLiveData
import tool.xfy9326.schedule.tools.livedata.postEvent
import tool.xfy9326.schedule.tools.livedata.setEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.schedule.ScheduleBackupHelper
import java.io.File

class AppErrorViewModel : AbstractViewModel() {
    val crashLog = MutableEventLiveData<String?>()
    val scheduleBackup = ScheduleBackupHelper(viewModelScope)

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