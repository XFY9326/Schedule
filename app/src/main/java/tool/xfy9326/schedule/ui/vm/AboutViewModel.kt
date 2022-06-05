package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.postEvent
import kotlinx.coroutines.launch
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