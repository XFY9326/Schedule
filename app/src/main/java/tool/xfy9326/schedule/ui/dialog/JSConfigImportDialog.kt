package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogEditTextBinding

class JSConfigImportDialog : DialogFragment() {
    companion object {
        private val DIALOG_TAG = JSConfigImportDialog::class.simpleName

        fun showDialog(fragmentManager: FragmentManager) {
            ImportCourseConflictDialog().show(fragmentManager, DIALOG_TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        val binding = DialogEditTextBinding.inflate(layoutInflater)
        setTitle(R.string.add_course_import)
        setView(binding.root)

        setPositiveButton(R.string.add) { _, _ ->

        }
        setNegativeButton(android.R.string.cancel, null)
        setNeutralButton(R.string.from_file) { _, _ ->

        }
    }.create()

    interface OnJSConfigImportListener {
        fun onJSConfigUrlImport(url: String)

        fun onJSConfigFileImport()
    }
}