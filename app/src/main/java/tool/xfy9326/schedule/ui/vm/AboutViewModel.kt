package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.io.FileManager
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class AboutViewModel : AbstractViewModel() {
    val showEULA = MutableEventLiveData<String>()
    val showOpenSourceLicense = MutableEventLiveData<String>()

    fun showEULA() {
        viewModelScope.launch {
            showEULA.postEvent(FileManager.readEULA())
        }
    }

    fun showOpenSourceLicense() {
        viewModelScope.launch {
            showOpenSourceLicense.postEvent(FileManager.readLicense())
        }
    }
}