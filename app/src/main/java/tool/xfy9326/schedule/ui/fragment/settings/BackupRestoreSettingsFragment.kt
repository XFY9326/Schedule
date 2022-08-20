package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import io.github.xfy9326.atools.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.BatchResult
import tool.xfy9326.schedule.kt.getParcelableCompat
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.MultiItemSelectDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.BackupUtils

class BackupRestoreSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val EXTRA_BATCH_RESULT = "EXTRA_BATCH_RESULT"
    }

    override val titleName: Int = R.string.backup_and_restore
    override val preferenceResId: Int = R.xml.settings_backup_restore
    private val backupSchedule = registerForActivityResult(ActivityResultContracts.CreateDocument(MIMEConst.MIME_APPLICATION_JSON)) {
        if (it != null) {
            requireSettingsViewModel()?.backupScheduleToUri(it)
        } else {
            requireSettingsViewModel()?.waitBackupScheduleId?.consume()
            requireRootLayout()?.showSnackBar(R.string.output_file_cancel)
        }
    }
    private val restoreSchedule = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            requireSettingsViewModel()?.restoreScheduleFromUri(it)
        }
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(R.string.pref_backup_schedule) {
            requireSettingsViewModel()?.getScheduleBackupList()
        }
        setOnPrefClickListener(R.string.pref_restore_schedule) {
            restoreSchedule.launch(MIMEConst.MIME_APPLICATION_JSON)
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.scheduleBackupList.observeEvent(viewLifecycleOwner) {
            MultiItemSelectDialog.showDialog(
                childFragmentManager,
                null,
                getString(R.string.backup_schedule_choose),
                showArr = it.map { min ->
                    min.name
                }.toTypedArray(),
                idArr = it.map { min ->
                    min.scheduleId
                }.toLongArray(),
                selectedArr = BooleanArray(it.size) { true }
            )
        }
        viewModel.backupScheduleToUriResult.observeEvent(viewLifecycleOwner) {
            requireRootLayout()?.showSnackBar(
                if (it) {
                    R.string.output_file_success
                } else {
                    R.string.output_file_failed
                }
            )
        }
        viewModel.restoreScheduleFromUriResult.observeEvent(viewLifecycleOwner) {
            if (it.second) {
                ImportCourseConflictDialog.showDialog(
                    childFragmentManager, bundleOf(
                        EXTRA_BATCH_RESULT to it.first
                    )
                )
            } else {
                showRestoreResult(it.first)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MultiItemSelectDialog.setOnMultiItemSelectedListener(childFragmentManager, viewLifecycleOwner) { _, idArr, selectedArr ->
            val idList = idArr.filterIndexed { i, _ ->
                selectedArr[i]
            }
            if (idList.isEmpty()) {
                requireRootLayout()?.showSnackBar(R.string.schedule_choose_empty)
            } else {
                requireSettingsViewModel()?.waitBackupScheduleId?.write(idList)
                backupSchedule.launch(BackupUtils.createBackupFileName(requireContext()))
            }
        }
        ImportCourseConflictDialog.setOnReadImportCourseConflictListener(childFragmentManager, viewLifecycleOwner) {
            it?.getParcelableCompat<BatchResult>(EXTRA_BATCH_RESULT)?.let(::showRestoreResult)
        }
    }

    private fun showRestoreResult(batchResult: BatchResult) {
        if (batchResult.success) {
            if (batchResult.failedAmount == 0) {
                requireRootLayout()?.showSnackBar(R.string.restore_schedule_success)
            } else {
                requireRootLayout()?.showSnackBar(R.string.restore_schedule_failed, batchResult.total, batchResult.failedAmount)
            }
        } else {
            requireRootLayout()?.showSnackBar(R.string.restore_schedule_error)
        }
    }
}