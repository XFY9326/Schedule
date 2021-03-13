package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.tools.livedata.MutableEventLiveData
import tool.xfy9326.schedule.tools.livedata.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.file.RawManager

class AboutViewModel : AbstractViewModel() {
    val showEULA = MutableEventLiveData<String>()
    val showOpenSourceLicense = MutableEventLiveData<String>()

    fun showEULA() {
        viewModelScope.launch {
            showEULA.postEvent(RawManager.readEULA())
        }
    }

    fun showOpenSourceLicense() {
        viewModelScope.launch {
            showOpenSourceLicense.postEvent(RawManager.readOpenSourceLicense())
        }
    }
}