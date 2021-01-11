package tool.xfy9326.schedule.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.requireOwner

class ImportCourseConflictDialog : DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        private val DIALOG_TAG = ImportCourseConflictDialog::class.simpleName

        fun showDialog(fragmentManager: FragmentManager) {
            ImportCourseConflictDialog().show(fragmentManager, DIALOG_TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.import_course_has_conflict)
        setMessage(R.string.import_course_has_conflict_msg)
        setPositiveButton(android.R.string.ok, this@ImportCourseConflictDialog)
    }.create()

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        requireOwner<OnConfirmImportCourseConflictListener>()?.onConfirmImportCourseConflict()
    }

    interface OnConfirmImportCourseConflictListener {
        fun onConfirmImportCourseConflict()
    }
}