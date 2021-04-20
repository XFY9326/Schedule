package tool.xfy9326.schedule.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.content.CourseImportConfigManager
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.tools.livedata.MutableNotifyLiveData
import tool.xfy9326.schedule.tools.livedata.notify
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class OnlineCourseImportViewModel : AbstractViewModel() {
    private val configManager = CourseImportConfigManager(viewModelScope)

    val courseImportConfigs = configManager.courseImportConfigs
    val preparedJSConfig = configManager.preparedJSConfig
    val jsConfigPrepareProgress = configManager.jsConfigPrepareProgress
    val configOperationError = configManager.configOperationError
    val configOperationAttention = configManager.configOperationAttention

    val onlineImportAttention = MutableNotifyLiveData()

    override fun onViewInitialized(firstInitialize: Boolean) {
        tryShowOnlineImportAttention()
    }

    private fun tryShowOnlineImportAttention() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!AppDataStore.readOnlineImportAttentionFlow.first()) onlineImportAttention.notify()
        }
    }

    fun hasReadOnlineImportAttention() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDataStore.setReadOnlineImportAttention(true)
        }
    }

    fun deleteJSConfig(jsConfig: JSConfig) {
        configManager.removeJSConfig(jsConfig)
    }

    fun addJSConfig(uri: Uri) {
        configManager.addJSConfig(uri)
    }

    fun addJSConfig(url: String) {
        configManager.addJSConfig(url)
    }

    fun prepareJSConfig(jsConfig: JSConfig) {
        configManager.prepareJSConfig(jsConfig)
    }
}