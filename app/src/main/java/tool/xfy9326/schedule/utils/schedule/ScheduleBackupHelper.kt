package tool.xfy9326.schedule.utils.schedule

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import io.github.xfy9326.atools.livedata.MutableEventLiveData
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.livedata.postEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.tools.DisposableValue
import tool.xfy9326.schedule.ui.dialog.MultiItemSelectDialog
import tool.xfy9326.schedule.utils.BackupUtils

class ScheduleBackupHelper(scope: CoroutineScope) : CoroutineScope by scope {
    enum class BackupStatus {
        SUCCESS,
        FAILED,
        EMPTY,
        CANCEL;
    }

    companion object {
        @StringRes
        fun getResultMsgId(status: BackupStatus): Int =
            when (status) {
                BackupStatus.SUCCESS -> R.string.output_file_success
                BackupStatus.FAILED -> R.string.output_file_failed
                BackupStatus.EMPTY -> R.string.schedule_choose_empty
                BackupStatus.CANCEL -> R.string.output_file_cancel
            }
    }

    private val backupStatus = MutableEventLiveData<BackupStatus>()
    private val scheduleList = MutableEventLiveData<List<Schedule.Min>>()
    private val waitBackupScheduleId = DisposableValue<List<Long>>()

    fun requestBackupScheduleList() {
        launch(Dispatchers.IO) {
            scheduleList.postEvent(ScheduleDBProvider.db.scheduleDAO.getScheduleMin().first())
        }
    }

    fun setupBackupView(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager,
        onBackupLaunch: () -> Unit,
        onBackupStatus: (BackupStatus) -> Unit
    ) {
        scheduleList.observeEvent(lifecycleOwner) {
            MultiItemSelectDialog.showDialog(
                fragmentManager,
                null,
                context.getString(R.string.backup_schedule_choose),
                showArr = it.map { min ->
                    min.name
                }.toTypedArray(),
                idArr = it.map { min ->
                    min.scheduleId
                }.toLongArray(),
                selectedArr = BooleanArray(it.size) { true }
            )
        }
        backupStatus.observeEvent(lifecycleOwner) {
            onBackupStatus(it)
        }

        MultiItemSelectDialog.setOnMultiItemSelectedListener(fragmentManager, lifecycleOwner) { _, idArr, selectedArr ->
            val idList = idArr.filterIndexed { i, _ ->
                selectedArr[i]
            }
            if (idList.isEmpty()) {
                onBackupStatus(BackupStatus.EMPTY)
            } else {
                waitBackupScheduleId.write(idList)
                onBackupLaunch()
            }
        }
    }

    fun backupToUri(outputUri: Uri?) {
        if (outputUri == null) {
            waitBackupScheduleId.consume()
            backupStatus.postEvent(BackupStatus.CANCEL)
        } else {
            launch {
                val idList = waitBackupScheduleId.read()
                val backupResult = idList?.let {
                    withContext(Dispatchers.Default) {
                        BackupUtils.backupSchedules(outputUri, it)
                    }
                } ?: false
                backupStatus.postEvent(if (backupResult) BackupStatus.SUCCESS else BackupStatus.FAILED)
            }
        }
    }
}