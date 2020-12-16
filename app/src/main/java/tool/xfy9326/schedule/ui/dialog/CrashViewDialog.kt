package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogCrashViewBinding
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.setWindowWidthPercent
import tool.xfy9326.schedule.kt.showGlobalShortToast

class CrashViewDialog : DialogFragment() {
    companion object {
        private const val LOG_TAG = "CrashLog"
        private const val ARGUMENT_CRASH_LOG = "CRASH_LOG"
        private const val ARGUMENT_OUTPUT_CRASH_LOG = "OUTPUT_CRASH_LOG"
        private const val WINDOW_WIDTH_PERCENT = 1.0

        fun showDialog(fragmentManager: FragmentManager, crashLog: String, outputCrashLog: Boolean = true) {
            CrashViewDialog().apply {
                arguments = buildBundle {
                    putString(ARGUMENT_CRASH_LOG, crashLog)
                    putBoolean(ARGUMENT_OUTPUT_CRASH_LOG, outputCrashLog)
                }
            }.show(fragmentManager, null)
        }
    }

    private lateinit var crashLog: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = requireArguments().getString(ARGUMENT_CRASH_LOG, null)
        if (crashLog == null) {
            showGlobalShortToast(R.string.crash_detail_not_found)
            dismiss()
            return
        } else {
            this.crashLog = crashLog
        }
    }

    override fun onCreateDialog(savedInstanceStae: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.crash_detail)
        val binding = DialogCrashViewBinding.inflate(layoutInflater)
        binding.textViewCrashDetail.text = crashLog
        setView(binding.root)
        setPositiveButton(R.string.print_error_log) { _, _ ->
            Log.e(LOG_TAG, crashLog)
        }
    }.create()

    override fun onStart() {
        super.onStart()
        dialog?.setWindowWidthPercent(WINDOW_WIDTH_PERCENT)
    }
}