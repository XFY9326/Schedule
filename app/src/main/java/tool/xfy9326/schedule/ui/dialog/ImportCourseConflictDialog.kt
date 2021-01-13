package tool.xfy9326.schedule.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.requireOwner
import java.io.Serializable

class ImportCourseConflictDialog : DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        private val DIALOG_TAG = ImportCourseConflictDialog::class.simpleName
        private const val EXTRA_PASSED_VALUE = "EXTRA_PASSED_VALUE"

        fun showDialog(fragmentManager: FragmentManager, passedValue: Serializable? = null) {
            ImportCourseConflictDialog().apply {
                arguments = buildBundle {
                    putSerializable(EXTRA_PASSED_VALUE, passedValue)
                }
            }.show(fragmentManager, DIALOG_TAG)
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
        requireOwner<OnConfirmImportCourseConflictListener>()?.onConfirmImportCourseConflict(
            requireArguments().getSerializable(EXTRA_PASSED_VALUE)
        )
    }

    interface OnConfirmImportCourseConflictListener {
        fun onConfirmImportCourseConflict(value: Serializable?)
    }
}