package tool.xfy9326.schedule.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lib.xfy9326.livedata.MutableNotifyLiveData
import lib.xfy9326.livedata.postNotify
import tool.xfy9326.schedule.content.CourseImportConfigManager
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class OnlineCourseImportViewModel : AbstractViewModel() {
    private val configManager = CourseImportConfigManager(viewModelScope)
    private var jsPrepareJob: Job? = null
    private var jsAddJob: Job? = null

    val courseImportConfigs = configManager.courseImportConfigs
    val preparedJSConfig = configManager.preparedJSConfig
    val jsConfigPrepareProgress = configManager.jsConfigPrepareProgress
    val configOperationError = configManager.configOperationError
    val configOperationAttention = configManager.configOperationAttention
    val configIgnorableWarning = configManager.configIgnorableWarning

    val onlineImportAttention = MutableNotifyLiveData()

    override fun onViewInitialized(firstInitialize: Boolean) {
        tryShowOnlineImportAttention()
    }

    private fun tryShowOnlineImportAttention() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!AppDataStore.readOnlineImportAttentionFlow.first()) onlineImportAttention.postNotify()
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
        jsAddJob?.cancel()
        jsAddJob = configManager.addJSConfig(uri)
    }

    fun addJSConfig(url: String) {
        jsAddJob?.cancel()
        jsAddJob = configManager.addJSConfig(url)
    }

    fun prepareJSConfig(jsConfig: JSConfig) {
        jsPrepareJob = configManager.prepareJSConfig(jsConfig)
    }

    fun cancelJSConfigAdd() {
        jsAddJob?.takeIf { !it.isCompleted }?.cancel()
        jsAddJob = null
    }

    fun cancelPrepareJSConfig() {
        jsPrepareJob?.takeIf { !it.isCompleted }?.cancel()
        jsPrepareJob = null
    }

    override fun onCleared() {
        cancelJSConfigAdd()
        cancelPrepareJSConfig()
        configManager.close()
    }
}