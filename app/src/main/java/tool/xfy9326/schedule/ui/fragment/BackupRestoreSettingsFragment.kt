package tool.xfy9326.schedule.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.kt.tryStartActivityForResult
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.MultiItemSelectDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.BackupUtils
import tool.xfy9326.schedule.utils.IntentUtils
import java.io.Serializable

@Suppress("unused")
class BackupRestoreSettingsFragment : AbstractSettingsFragment(), MultiItemSelectDialog.OnMultiItemSelectedListener,
    ImportCourseConflictDialog.OnConfirmImportCourseConflictListener {
    companion object {
        private const val PREFERENCE_BACKUP_SCHEDULE = "backupSchedule"
        private const val PREFERENCE_RESTORE_SCHEDULE = "restoreSchedule"

        private const val REQUEST_CODE_BACKUP_SCHEDULE = 1
        private const val REQUEST_CODE_RESTORE_SCHEDULE = 2
    }

    override val titleName: Int = R.string.backup_and_restore
    override val preferenceResId: Int = R.xml.settings_backup_restore

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(PREFERENCE_BACKUP_SCHEDULE) {
            requireSettingsViewModel()?.getScheduleBackupList()
        }
        setOnPrefClickListener(PREFERENCE_RESTORE_SCHEDULE) {
            tryStartActivityForResult(IntentUtils.getSelectJsonFromDocumentIntent(), REQUEST_CODE_RESTORE_SCHEDULE)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireSettingsViewModel()?.apply {
            scheduleBackupList.observeEvent(this@BackupRestoreSettingsFragment) {
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
            backupScheduleToUriResult.observeEvent(this@BackupRestoreSettingsFragment) {
                requireRootLayout()?.showShortSnackBar(
                    if (it) {
                        R.string.output_file_success
                    } else {
                        R.string.output_file_failed
                    }
                )
            }
            restoreScheduleFromUriResult.observeEvent(this@BackupRestoreSettingsFragment) {
                if (it.second) {
                    ImportCourseConflictDialog.showDialog(childFragmentManager, it.first)
                } else {
                    showRestoreResult(it.first)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_BACKUP_SCHEDULE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val outputUri = data?.data
                    if (outputUri != null) {
                        requireSettingsViewModel()?.backupScheduleToUri(outputUri)
                    } else {
                        requireRootLayout()?.showShortSnackBar(R.string.output_file_create_failed)
                    }
                } else {
                    requireSettingsViewModel()?.waitBackupScheduleId?.consume()
                    requireRootLayout()?.showShortSnackBar(R.string.output_file_cancel)
                }
            }
            REQUEST_CODE_RESTORE_SCHEDULE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val outputUri = data?.data
                    if (outputUri != null) {
                        requireSettingsViewModel()?.restoreScheduleFromUri(outputUri)
                    } else {
                        requireRootLayout()?.showShortSnackBar(R.string.input_file_found_failed)
                    }
                } else {
                    requireRootLayout()?.showShortSnackBar(R.string.input_file_cancel)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConfirmImportCourseConflict(value: Serializable?) {
        (value as? BatchResult)?.let(::showRestoreResult)
    }

    override fun onMultiItemSelected(tag: String?, idArr: LongArray, selectedArr: BooleanArray) {
        val idList = idArr.filterIndexed { i, _ ->
            selectedArr[i]
        }
        if (idList.isEmpty()) {
            requireRootLayout()?.showShortSnackBar(R.string.schedule_choose_empty)
        } else {
            requireSettingsViewModel()?.waitBackupScheduleId?.write(idList)
            tryStartActivityForResult(
                IntentUtils.getCreateNewDocumentIntent(BackupUtils.createBackupFileName(requireContext()), MIMEConst.MIME_APPLICATION_JSON),
                REQUEST_CODE_BACKUP_SCHEDULE
            )
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