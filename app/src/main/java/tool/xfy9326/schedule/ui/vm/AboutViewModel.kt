package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.PathManager

class AboutViewModel : AbstractViewModel() {
    val showEULA = MutableEventLiveData<String>()
    val showOpenSourceLicense = MutableEventLiveData<String>()

    fun showEULA() {
        viewModelScope.launch {
            PathManager.ASSETS_EULA_FILE_READER.read()?.let {
                showEULA.postEvent(it)
            }
        }
    }

    fun showOpenSourceLicense() {
        viewModelScope.launch {
            PathManager.ASSETS_LICENSE_FILE_READER.read()?.let {
                showOpenSourceLicense.postEvent(it)
            }
        }
    }
}