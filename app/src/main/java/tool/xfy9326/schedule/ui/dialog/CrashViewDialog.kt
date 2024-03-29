package tool.xfy9326.schedule.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.io.okio.writeText
import io.github.xfy9326.atools.ui.showGlobalToast
import io.github.xfy9326.atools.ui.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.DialogCrashViewBinding
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.utils.PROJECT_ID
import tool.xfy9326.schedule.utils.setWindowPercent

class CrashViewDialog : AppCompatDialogFragment() {
    companion object {
        private const val LOG_TAG = "CrashLog"
        private const val ARGUMENT_CRASH_LOG = "CRASH_LOG"
        private const val ARGUMENT_OUTPUT_CRASH_LOG = "OUTPUT_CRASH_LOG"
        private const val WINDOW_WIDTH_PERCENT = 1.0

        private const val DEFAULT_EXPORT_LOG_NAME = "${PROJECT_ID}_ExportLog_%d.log"

        fun showDialog(fragmentManager: FragmentManager, crashLog: String, outputCrashLog: Boolean = true) {
            CrashViewDialog().apply {
                arguments = bundleOf(
                    ARGUMENT_CRASH_LOG to crashLog,
                    ARGUMENT_OUTPUT_CRASH_LOG to outputCrashLog
                )
            }.show(fragmentManager, null)
        }
    }

    private lateinit var crashLog: String
    private val outputLogFile = registerForActivityResult(ActivityResultContracts.CreateDocument(MIMEConst.MIME_TEXT)) {
        if (it != null) {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        it.writeText(requireArguments().getString(ARGUMENT_CRASH_LOG, null))
                    }.isSuccess
                }
                requireContext().showToast(if (result) R.string.output_file_success else R.string.output_file_failed)
                dismissAllowingStateLoss()
            }
        } else {
            requireContext().showToast(R.string.output_file_cancel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashLog = requireArguments().getString(ARGUMENT_CRASH_LOG, null)
        if (crashLog == null) {
            showGlobalToast(R.string.crash_detail_not_found)
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
        setNeutralButton(R.string.export, null)
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(R.string.print_error_log, null)
        setView(binding.root)
    }.create().also { dialog ->
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                outputLogFile.launch(DEFAULT_EXPORT_LOG_NAME.format(System.currentTimeMillis()))
            }
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                Log.e(LOG_TAG, crashLog)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().setWindowPercent(WINDOW_WIDTH_PERCENT)
    }
}