package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.json.upgrade.UpdateInfo
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.DirUtils
import tool.xfy9326.schedule.utils.UpgradeUtils
import java.io.File

class AppErrorViewModel : AbstractViewModel() {
    val crashLog = MutableEventLiveData<String?>()
    val updateInfo = MutableEventLiveData<Pair<Boolean, UpdateInfo?>>()

    fun loadCrashLogDetail(crashLogFileName: String?) = viewModelScope.launch {
        if (crashLogFileName == null) {
            crashLog.postEvent(null)
        } else {
            crashLog.postEvent(TextIO.readText(File(DirUtils.LogDir, crashLogFileName)))
        }
    }

    fun checkUpgrade() {
        viewModelScope.launch(Dispatchers.Default) {
            updateInfo.postEvent(UpgradeUtils.check(true))
        }
    }
}