package tool.xfy9326.schedule.ui.vm

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.io.BaseIO
import tool.xfy9326.schedule.io.BaseIO.deleteFile
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.asParentOf
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.kt.weak
import tool.xfy9326.schedule.tools.DisposableValue
import tool.xfy9326.schedule.tools.ExceptionHandler
import tool.xfy9326.schedule.tools.ImageHelper
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.BackupUtils
import tool.xfy9326.schedule.utils.DirUtils
import tool.xfy9326.schedule.utils.ScheduleSyncHelper

class SettingsViewModel : AbstractViewModel() {
    private val scheduleSyncFlow = ScheduleDBProvider.db.scheduleSyncDao.getScheduleSyncsInfo().map {
        ArrayList<Pair<Schedule.Min, ScheduleSync>>().apply {
            for (info in it) {
                val syncInfo = info.syncInfo ?: AppSettingsDataStore.getDefaultScheduleSyncFlow(info.scheduleMin.scheduleId).first()
                add(info.scheduleMin to syncInfo)
            }
        }
    }

    val importScheduleImage by lazy { MutableEventLiveData<Boolean>() }
    val readDebugLogs by lazy { MutableEventLiveData<Array<String>>() }
    val outputDebugLogs by lazy { MutableEventLiveData<Array<String>>() }
    val showDebugLog by lazy { MutableEventLiveData<String>() }
    val outputLogFileToUriResult by lazy { MutableEventLiveData<Boolean>() }
    val backupScheduleToUriResult by lazy { MutableEventLiveData<Boolean>() }
    val restoreScheduleFromUriResult by lazy { MutableEventLiveData<Pair<BatchResult, Boolean>>() }
    val syncToCalendarStatus by lazy { MutableEventLiveData<BatchResult>() }
    val scheduleSyncEdit by lazy { MutableEventLiveData<Pair<String, List<Pair<Schedule.Min, ScheduleSync>>>>() }
    val scheduleBackupList by lazy { MutableEventLiveData<List<Schedule.Min>>() }

    val waitCreateLogFileName by lazy { DisposableValue<String>() }
    val waitBackupScheduleId by lazy { DisposableValue<List<Long>>() }

    fun getScheduleBackupList() {
        viewModelScope.launch {
            scheduleBackupList.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first())
        }
    }

    fun backupScheduleToUri(context: Context, outputUri: Uri) {
        val weakContext = context.weak()
        viewModelScope.launch(Dispatchers.Default) {
            val idList = waitBackupScheduleId.read()
            if (idList != null) {
                weakContext.get()?.let {
                    backupScheduleToUriResult.postEvent(
                        BackupUtils.backupSchedules(it, outputUri, idList)
                    )
                }
            } else {
                backupScheduleToUriResult.postEvent(false)
            }
        }
    }

    fun restoreScheduleFromUri(context: Context, outputUri: Uri) {
        val weakContext = context.weak()
        viewModelScope.launch(Dispatchers.Default) {
            weakContext.get()?.let {
                restoreScheduleFromUriResult.postEvent(BackupUtils.restoreSchedules(it, outputUri))
            }
        }
    }

    fun getScheduleSyncEditList(key: String) {
        viewModelScope.launch {
            scheduleSyncEdit.postEvent(key to scheduleSyncFlow.first())
        }
    }

    fun updateSyncEnabled(scheduleIds: LongArray, enabledArr: BooleanArray) {
        editSyncInfo(scheduleIds, enabledArr) { sync, enabled ->
            sync.syncable = enabled
        }
    }

    fun updateSyncEditable(scheduleIds: LongArray, enabledArr: BooleanArray) {
        editSyncInfo(scheduleIds, enabledArr) { sync, enabled ->
            sync.editable = enabled
        }
    }

    fun updateSyncVisible(scheduleIds: LongArray, enabledArr: BooleanArray) {
        editSyncInfo(scheduleIds, enabledArr) { sync, enabled ->
            sync.defaultVisible = enabled
        }
    }

    fun updateSyncReminder(scheduleIds: LongArray, enabledArr: BooleanArray) {
        editSyncInfo(scheduleIds, enabledArr) { sync, enabled ->
            sync.addReminder = enabled
        }
    }

    private fun editSyncInfo(scheduleIds: LongArray, enabledArr: BooleanArray, edit: (ScheduleSync, Boolean) -> Unit) {
        viewModelScope.launch {
            ScheduleDBProvider.db.scheduleSyncDao.apply {
                for (i in scheduleIds.indices) {
                    val syncInfo = getScheduleSync(scheduleIds[i]).first()
                    val oldHashCode = syncInfo.hashCode()
                    edit(syncInfo, enabledArr[i])
                    if (syncInfo.hashCode() != oldHashCode) {
                        updateScheduleSync(syncInfo)
                    }
                }
            }
        }
    }

    fun clearCalendar(context: Context) {
        val weakContext = context.weak()
        viewModelScope.launch {
            weakContext.get()?.let {
                ScheduleSyncHelper.removeAllCalendar(it)
            }
        }
    }

    fun clearCalendarSettings() {
        viewModelScope.launch {
            ScheduleDBProvider.db.scheduleSyncDao.clearAll()
        }
    }

    fun syncToCalendar(context: Context) {
        val weakContext = context.weak()
        viewModelScope.launch(Dispatchers.Default) {
            weakContext.get()?.let {
                ScheduleSyncHelper.syncCalendar(it)?.let(syncToCalendarStatus::postEvent)
            }
        }
    }

    fun importScheduleImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
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
        val weakContext = context.weak()
        viewModelScope.launch(Dispatchers.IO) {
            val logName = waitCreateLogFileName.read()
            if (logName != null) {
                weakContext.get()?.let {
                    outputLogFileToUriResult.postEvent(BaseIO.writeFileToUri(it, outputUri, DirUtils.LogDir.asParentOf(logName)))
                }
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
        val weakContext = context.weak()
        viewModelScope.launch {
            weakContext.get()?.let { context ->
                context.cacheDir?.deleteFile()
                ContextCompat.getCodeCacheDir(context)?.deleteFile()
                ContextCompat.getExternalCacheDirs(context).forEach {
                    it?.deleteFile()
                }
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
            ScheduleDBProvider.db.scheduleSyncDao.clearAll()
        }
    }
}