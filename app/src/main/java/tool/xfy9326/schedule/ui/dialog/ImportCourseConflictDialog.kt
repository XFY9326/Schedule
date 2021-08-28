package tool.xfy9326.schedule.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R

class ImportCourseConflictDialog : AppCompatDialogFragment(), DialogInterface.OnClickListener {
    companion object {
        private val DIALOG_TAG = ImportCourseConflictDialog::class.java.simpleName

        fun showDialog(fragmentManager: FragmentManager, value: Bundle? = null) {
            ImportCourseConflictDialog().apply {
                arguments = value
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun setOnReadImportCourseConflictListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, block: (Bundle?) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(bundle.takeUnless { it.isEmpty })
            }
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
        setFragmentResult(DIALOG_TAG, arguments ?: Bundle.EMPTY)
    }
}