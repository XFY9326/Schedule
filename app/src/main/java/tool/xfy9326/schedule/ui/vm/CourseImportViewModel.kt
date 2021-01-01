package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.content.CourseAdapterConfig
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel

class CourseImportViewModel : AbstractViewModel() {
    val courseMetas = MutableLiveData<List<CourseImportConfig<*, *>>>()

    fun loadCourseImportMetas() {
        viewModelScope.launch {
            courseMetas.postValue(CourseAdapterConfig.metas)
        }
    }
}