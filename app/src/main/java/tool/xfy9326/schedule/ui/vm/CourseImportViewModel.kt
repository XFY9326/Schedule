package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.content.CourseAdapterConfig
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.kt.MutableNotifyLiveData
import tool.xfy9326.schedule.kt.notify
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class CourseImportViewModel : AbstractViewModel() {
    val courseMetas = MutableLiveData<List<CourseImportConfig<*, *>>>()
    val onlineImportAttention = MutableNotifyLiveData()

    fun loadCourseImportMetas() {
        viewModelScope.launch(Dispatchers.Default) {
            courseMetas.postValue(CourseAdapterConfig.getConfigs())
        }
    }

    fun tryShowOnlineImportAttention() {
        viewModelScope.launch {
            if (!AppDataStore.readOnlineImportAttentionFlow.first()) onlineImportAttention.notify()
        }
    }

    fun hasReadOnlineImportAttention() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDataStore.setReadOnlineImportAttention(true)
        }
    }
}