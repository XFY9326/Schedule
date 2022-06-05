package tool.xfy9326.schedule.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.MutableNotifyLiveData
import io.github.xfy9326.atools.livedata.postEvent
import io.github.xfy9326.atools.livedata.postNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.content.CourseImportConfigManager
import tool.xfy9326.schedule.content.beans.JSConfig
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.AppSettingsDataStore
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
    val jsConfigExistWarning = configManager.jsConfigExistWarning

    val onlineImportAttention = MutableNotifyLiveData()
    val launchJSConfig = MutableEventLiveData<Pair<JSConfig, Boolean>>()

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

    fun launchJSConfig(jsConfig: JSConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            launchJSConfig.postEvent(jsConfig to AppSettingsDataStore.jsCourseImportEnableNetworkFlow.first())
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

    fun forceAddJSConfig(jsConfig: JSConfig) {
        jsAddJob?.cancel()
        jsAddJob = configManager.addJSConfig(jsConfig, true)
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