package tool.xfy9326.schedule.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import lib.xfy9326.kit.asParentOf
import lib.xfy9326.livedata.MutableEventLiveData
import lib.xfy9326.livedata.postEvent
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.SchedulePreviewStyles
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.io.CrashFileManager
import tool.xfy9326.schedule.io.IOManager
import tool.xfy9326.schedule.io.PathManager
import tool.xfy9326.schedule.io.utils.ImageUtils
import tool.xfy9326.schedule.kt.asDistinctLiveData
import tool.xfy9326.schedule.tools.DisposableValue
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.BackupUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleDataProcessor
import tool.xfy9326.schedule.utils.schedule.ScheduleSyncHelper

class SettingsViewModel : AbstractViewModel() {
    private val scheduleSyncFlow = ScheduleDBProvider.db.scheduleSyncDao.getScheduleSyncsInfo().map {
        ArrayList<Pair<Schedule.Min, ScheduleSync>>().apply {
            for (info in it) {
                val syncInfo = info.syncInfo ?: AppSettingsDataStore.getDefaultScheduleSyncFlow(info.scheduleMin.scheduleId).first()
                add(info.scheduleMin to syncInfo)
            }
        }
    }

    val schedulePreviewStyles by lazy {
        ScheduleDataStore.defaultFirstDayOfWeekFlow.combine(ScheduleDataStore.scheduleStylesFlow) { weekStart, styles ->
            SchedulePreviewStyles(weekStart, styles)
        }.asDistinctLiveData()
    }
    val scheduleBackground by lazy { ScheduleDataProcessor.scheduleBackgroundFlow.asDistinctLiveData() }
    val schedulePreviewPreviewWidth by lazy { MutableEventLiveData<Boolean>() }

    val importScheduleImage by lazy { MutableEventLiveData<Boolean>() }
    val allDebugLogs by lazy { MutableEventLiveData<Pair<String, List<String>>>() }
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
        viewModelScope.launch(Dispatchers.IO) {
            scheduleBackupList.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first())
        }
    }

    fun backupScheduleToUri(outputUri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val idList = waitBackupScheduleId.read()
            if (idList != null) {
                backupScheduleToUriResult.postEvent(BackupUtils.backupSchedules(outputUri, idList))
            } else {
                backupScheduleToUriResult.postEvent(false)
            }
        }
    }

    fun restoreScheduleFromUri(outputUri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            restoreScheduleFromUriResult.postEvent(BackupUtils.restoreSchedules(outputUri))
        }
    }

    fun getScheduleSyncEditList(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
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

    fun clearCalendar() {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleSyncHelper.removeAllCalendar()
        }
    }

    fun clearCalendarSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleDBProvider.db.scheduleSyncDao.clearAll()
        }
    }

    fun syncToCalendar() {
        viewModelScope.launch(Dispatchers.Default) {
            ScheduleSyncHelper.syncCalendar()?.let(syncToCalendarStatus::postEvent)
        }
    }

    fun importScheduleImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val quality = ScheduleDataStore.scheduleBackgroundImageQualityFlow.first()
            val imageName = ImageUtils.importImageFromUri(uri, PathManager.PictureAppDir, quality)
            if (imageName == null) {
                importScheduleImage.postEvent(false)
            } else {
                ScheduleDataStore.scheduleBackgroundImageFlow.firstOrNull()?.let {
                    PathManager.PictureAppDir.asParentOf(it).delete()
                }
                ScheduleDataStore.setScheduleBackgroundImage(imageName)
                importScheduleImage.postEvent(true)
            }
        }
    }

    fun outputLogFileToUri(outputUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val logName = waitCreateLogFileName.read()
            if (logName != null) {
                outputLogFileToUriResult.postEvent(CrashFileManager.copyLogFile(logName, outputUri))
            } else {
                outputLogFileToUriResult.postEvent(false)
            }
        }
    }

    fun showDebugLog(logName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CrashFileManager.readCrashLog(logName)?.let {
                showDebugLog.postEvent(it)
            }
        }
    }

    fun getAllLogs(action: String) {
        viewModelScope.launch(Dispatchers.IO) {
            allDebugLogs.postEvent(action to CrashFileManager.getAllDebugLogsName())
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            IOManager.clearAllCache()
        }
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            PathManager.LogDir.deleteRecursively()
        }
    }

    fun restoreSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            AppSettingsDataStore.clear()
            ScheduleDataStore.clear()
            ScheduleDBProvider.db.scheduleSyncDao.clearAll()
        }
    }
}