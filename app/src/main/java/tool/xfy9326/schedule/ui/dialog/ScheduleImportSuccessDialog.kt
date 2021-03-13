package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.requireOwner

class ScheduleImportSuccessDialog : DialogFragment() {
    companion object {
        private val DIALOG_TAG = ScheduleImportSuccessDialog::class.simpleName
        private const val EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"

        fun showDialog(fragmentManager: FragmentManager, scheduleId: Long) {
            ScheduleImportSuccessDialog().apply {
                arguments = bundleOf(
                    EXTRA_SCHEDULE_ID to scheduleId
                )
            }.show(fragmentManager, DIALOG_TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.course_import_success_title)
        setMessage(R.string.course_import_success_attention)
        setPositiveButton(R.string.edit_schedule_now) { _, _ ->
            requireOwner<OnScheduleImportSuccessListener>()?.onEditScheduleNow(requireArguments().getLong(EXTRA_SCHEDULE_ID))
        }
        setNegativeButton(R.string.edit_schedule_later) { _, _ ->
            requireOwner<OnScheduleImportSuccessListener>()?.onEditScheduleLater()
        }
    }.create()

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    interface OnScheduleImportSuccessListener {
        fun onEditScheduleNow(scheduleId: Long)

        fun onEditScheduleLater()
    }
}