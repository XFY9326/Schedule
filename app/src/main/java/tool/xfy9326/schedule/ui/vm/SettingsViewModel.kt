package tool.xfy9326.schedule.ui.vm

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.io.BaseIO
import tool.xfy9326.schedule.io.BaseIO.deleteFile
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.asParentOf
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.tools.DisposableValue
import tool.xfy9326.schedule.tools.ExceptionHandler
import tool.xfy9326.schedule.tools.ImageHelper
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.DirUtils

class SettingsViewModel : AbstractViewModel() {
    val importScheduleImage = MutableEventLiveData<Boolean>()
    val readDebugLogs = MutableEventLiveData<Array<String>>()
    val outputDebugLogs = MutableEventLiveData<Array<String>>()
    val showDebugLog = MutableEventLiveData<String>()
    val outputLogFileToUriResult = MutableEventLiveData<Boolean>()

    val waitCreateLogFileName = DisposableValue<String>()

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

    fun outputLogFileToUri(context: Context, outputUri: Uri) {
        viewModelScope.launch {
            val logName = waitCreateLogFileName.read()
            if (logName != null) {
                outputLogFileToUriResult.postEvent(BaseIO.writeFileToUri(context, outputUri, DirUtils.LogDir.asParentOf(logName)))
            } else {
                outputLogFileToUriResult.postEvent(false)
            }
        }
    }

    fun showDebugLog(log: String) {
        viewModelScope.launch {
            TextIO.readText(DirUtils.LogDir.asParentOf(log))?.let {
                showDebugLog.postEvent(it)
            }
        }
    }

    fun readDebugLogs() {
        viewModelScope.launch {
            readDebugLogs.postEvent(ExceptionHandler.getAllDebugLogs())
        }
    }

    fun outputDebugLogs() {
        viewModelScope.launch {
            outputDebugLogs.postEvent(ExceptionHandler.getAllDebugLogs())
        }
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            context.cacheDir?.deleteFile()
            ContextCompat.getCodeCacheDir(context)?.deleteFile()
            ContextCompat.getExternalCacheDirs(context).forEach {
                it?.deleteFile()
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            DirUtils.LogDir.deleteRecursively()
        }
    }

    fun restoreSettings() {
        viewModelScope.launch {
            AppSettingsDataStore.clear()
            ScheduleDataStore.clear()
        }
    }
}