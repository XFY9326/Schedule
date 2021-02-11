package tool.xfy9326.schedule.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogCrashViewBinding
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.*

class CrashViewDialog : AppCompatDialogFragment() {
    companion object {
        private const val LOG_TAG = "CrashLog"
        private const val ARGUMENT_CRASH_LOG = "CRASH_LOG"
        private const val ARGUMENT_OUTPUT_CRASH_LOG = "OUTPUT_CRASH_LOG"
        private const val WINDOW_WIDTH_PERCENT = 1.0

        private const val DEFAULT_EXPORT_LOG_NAME = "${APP_ID}_ExportLog_%d.log"

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
    private val outputLogFile = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) { TextIO.writeText(requireArguments().getString(ARGUMENT_CRASH_LOG, null), it) }
                showShortToast(if (result) R.string.output_file_success else R.string.output_file_failed)
                dismissAllowingStateLoss()
            }
        } else {
            showShortToast(R.string.output_file_cancel)
        }
    }

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
        setNeutralButton(R.string.export, null)
        setPositiveButton(R.string.print_error_log) { _, _ ->
            Log.e(LOG_TAG, crashLog)
        }
    }.create().also { dialog ->
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                outputLogFile.launch(DEFAULT_EXPORT_LOG_NAME.format(System.currentTimeMillis()))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setWindowWidthPercent(WINDOW_WIDTH_PERCENT)
    }
}