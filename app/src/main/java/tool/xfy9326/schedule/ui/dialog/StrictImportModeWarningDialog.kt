package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R

class StrictImportModeWarningDialog : AppCompatDialogFragment() {
    companion object {
        private val DIALOG_TAG = StrictImportModeWarningDialog::class.simpleName
        private const val EXTRA_ERROR_TEXT = "EXTRA_ERROR_TEXT"
        private const val EXTRA_ERROR_LOG = "EXTRA_ERROR_LOG"

        fun showDialog(fragmentManager: FragmentManager, errorText: String, errorLog: String?) {
            StrictImportModeWarningDialog().apply {
                arguments = bundleOf(
                    EXTRA_ERROR_TEXT to errorText,
                    EXTRA_ERROR_LOG to errorLog
                )
            }.show(fragmentManager, DIALOG_TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.schedule_import_strict_mode)
        setMessage(getString(R.string.schedule_import_strict_mode_msg, requireArguments().getString(EXTRA_ERROR_TEXT)))
        setPositiveButton(android.R.string.ok, null)

        val log = requireArguments().getString(EXTRA_ERROR_LOG)
        if (log != null) {
            setNeutralButton(R.string.read_debug_logs) { _, _ ->
                CrashViewDialog.showDialog(parentFragmentManager, log)
            }
        }
    }.create()

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }
}