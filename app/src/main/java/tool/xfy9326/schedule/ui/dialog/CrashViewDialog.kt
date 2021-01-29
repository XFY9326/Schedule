package tool.xfy9326.schedule.ui.dialog

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
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
import tool.xfy9326.schedule.utils.IntentUtils

class CrashViewDialog : DialogFragment() {
    companion object {
        private const val LOG_TAG = "CrashLog"
        private const val ARGUMENT_CRASH_LOG = "CRASH_LOG"
        private const val ARGUMENT_OUTPUT_CRASH_LOG = "OUTPUT_CRASH_LOG"
        private const val WINDOW_WIDTH_PERCENT = 1.0

        private const val REQUEST_CODE_CREATE_LOG_DOCUMENT = 1080
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
                val newLogName = DEFAULT_EXPORT_LOG_NAME.format(System.currentTimeMillis())
                tryStartActivityForResult(IntentUtils.getCreateNewDocumentIntent(newLogName), REQUEST_CODE_CREATE_LOG_DOCUMENT)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setWindowWidthPercent(WINDOW_WIDTH_PERCENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CREATE_LOG_DOCUMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val outputUri = data?.data
                if (outputUri != null) {
                    lifecycleScope.launch {
                        val result = withContext(Dispatchers.IO) { TextIO.writeText(crashLog, outputUri) }
                        showShortToast(
                            if (result) {
                                R.string.output_file_success
                            } else {
                                R.string.output_file_failed
                            }
                        )
                        dismissAllowingStateLoss()
                    }
                } else {
                    showShortToast(R.string.output_file_create_failed)
                }
            } else {
                showShortToast(R.string.output_file_cancel)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}