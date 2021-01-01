package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.DirUtils

class AboutViewModel : AbstractViewModel() {
    val showEULA = MutableEventLiveData<String>()
    val showOpenSourceLicense = MutableEventLiveData<String>()

    fun showEULA() {
        viewModelScope.launch {
            TextIO.readAssetFileAsText(App.instance, DirUtils.ASSETS_EULA_FILE)?.let {
                showEULA.postEvent(it)
            }
        }
    }

    fun showOpenSourceLicense() {
        viewModelScope.launch {
            TextIO.readAssetFileAsText(App.instance, DirUtils.ASSETS_LICENSE_FILE)?.let {
                showOpenSourceLicense.postEvent(it)
            }
        }
    }
}