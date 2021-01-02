package tool.xfy9326.schedule.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.asParentOf
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.tools.ImageHelper
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.DirUtils

class SettingsViewModel : AbstractViewModel() {
    val importScheduleImage = MutableEventLiveData<Boolean>()

    fun importScheduleImage(uri: Uri) {
        viewModelScope.launch {
            val quality = ScheduleDataStore.scheduleBackgroundImageQualityFlow.first()
            val imageName = ImageHelper.importImageFromUri(App.instance, uri, DirUtils.PictureAppDir, quality)
            if (imageName == null) {
                importScheduleImage.postEvent(false)
            } else {
                ScheduleDataStore.scheduleBackgroundImageFlow.firstOrNull()?.let {
                    DirUtils.PictureAppDir.asParentOf(it).delete()
                }
                ScheduleDataStore.setScheduleBackgroundImage(imageName)
                importScheduleImage.postEvent(true)
            }
        }
    }
}