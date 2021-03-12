package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.tools.livedata.observeEvent
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.MultiItemSelectDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.BackupUtils

@Suppress("unused")
class BackupRestoreSettingsFragment : AbstractSettingsFragment(), MultiItemSelectDialog.OnMultiItemSelectedListener,
    ImportCourseConflictDialog.OnConfirmImportCourseConflictListener<BatchResult> {
    companion object {
        private const val PREFERENCE_BACKUP_SCHEDULE = "backupSchedule"
        private const val PREFERENCE_RESTORE_SCHEDULE = "restoreSchedule"
    }

    override val titleName: Int = R.string.backup_and_restore
    override val preferenceResId: Int = R.xml.settings_backup_restore
    private val backupSchedule = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            requireSettingsViewModel()?.backupScheduleToUri(it)
        } else {
            requireSettingsViewModel()?.waitBackupScheduleId?.consume()
            requireRootLayout()?.showShortSnackBar(R.string.output_file_cancel)
        }
    }
    private val restoreSchedule = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            requireSettingsViewModel()?.restoreScheduleFromUri(it)
        } else {
            requireRootLayout()?.showShortSnackBar(R.string.input_file_cancel)
        }
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(PREFERENCE_BACKUP_SCHEDULE) {
            requireSettingsViewModel()?.getScheduleBackupList()
        }
        setOnPrefClickListener(PREFERENCE_RESTORE_SCHEDULE) {
            restoreSchedule.launch(MIMEConst.MIME_APPLICATION_JSON)
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.scheduleBackupList.observeEvent(this) {
            MultiItemSelectDialog.showDialog(
                childFragmentManager,
                PREFERENCE_BACKUP_SCHEDULE,
                getString(R.string.backup_schedule_choose),
                showArr = it.map { min ->
                    min.name
                }.toTypedArray(),
                idArr = it.map { min ->
                    min.scheduleId
                }.toLongArray(),
                selectedArr = BooleanArray(it.size) { false }
            )
        }
        viewModel.backupScheduleToUriResult.observeEvent(this) {
            requireRootLayout()?.showShortSnackBar(
                if (it) {
                    R.string.output_file_success
                } else {
                    R.string.output_file_failed
                }
            )
        }
        viewModel.restoreScheduleFromUriResult.observeEvent(this) {
            if (it.second) {
                ImportCourseConflictDialog.showDialog(childFragmentManager, it.first)
            } else {
                showRestoreResult(it.first)
            }
        }
    }

    override fun onConfirmImportCourseConflict(value: BatchResult?) {
        value?.let(::showRestoreResult)
    }

    override fun onMultiItemSelected(tag: String?, idArr: LongArray, selectedArr: BooleanArray) {
        val idList = idArr.filterIndexed { i, _ ->
            selectedArr[i]
        }
        if (idList.isEmpty()) {
            requireRootLayout()?.showShortSnackBar(R.string.schedule_choose_empty)
        } else {
            requireSettingsViewModel()?.waitBackupScheduleId?.write(idList)
            backupSchedule.launch(BackupUtils.createBackupFileName(requireContext()))
        }
    }

    private fun showRestoreResult(batchResult: BatchResult) {
        if (batchResult.success) {
            if (batchResult.failedAmount == 0) {
                requireRootLayout()?.showShortSnackBar(R.string.restore_schedule_success)
            } else {
                requireRootLayout()?.showShortSnackBar(R.string.restore_schedule_failed, batchResult.total, batchResult.failedAmount)
            }
        } else {
            requireRootLayout()?.showShortSnackBar(R.string.restore_schedule_error)
        }
    }
}