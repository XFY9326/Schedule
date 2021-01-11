package tool.xfy9326.schedule.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.show
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.IntentUtils

@Suppress("unused")
class DebugSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val KEY_READ_DEBUG_LOGS = "readDebugLogs"
        private const val KEY_OUTPUT_DEBUG_LOGS = "outputDebugLogs"
        private const val KEY_CLEAR_DEBUG_LOGS = "clearDebugLogs"

        private const val REQUEST_CODE_CREATE_LOG_DOCUMENT = 1
    }

    override val titleName: Int = R.string.debug_settings
    override val preferenceResId: Int = R.xml.settings_debug
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(KEY_READ_DEBUG_LOGS) {
            requireSettingsViewModel()?.readDebugLogs()
        }
        setOnPrefClickListener(KEY_OUTPUT_DEBUG_LOGS) {
            requireSettingsViewModel()?.outputDebugLogs()
        }
        setOnPrefClickListener(KEY_CLEAR_DEBUG_LOGS) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_debug_logs)
                setMessage(R.string.clear_debug_logs_msg)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearLogs()
                    requireRootLayout()?.showShortSnackBar(R.string.clear_debug_logs_success)
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireSettingsViewModel()?.apply {
            readDebugLogs.observeEvent(this@DebugSettingsFragment) {
                showDebugLogsSelectDialog(it, R.string.read_debug_logs) { log ->
                    showDebugLog(log)
                }
            }
            outputDebugLogs.observeEvent(this@DebugSettingsFragment) {
                showDebugLogsSelectDialog(it, R.string.output_debug_logs) { log ->
                    waitCreateLogFileName.write(log)
                    startActivityForResult(IntentUtils.getCreateNewDocumentIntent(log), REQUEST_CODE_CREATE_LOG_DOCUMENT)
                }
            }
            showDebugLog.observeEvent(this@DebugSettingsFragment) {
                CrashViewDialog.showDialog(childFragmentManager, it)
            }
            outputLogFileToUriResult.observeEvent(this@DebugSettingsFragment) {
                requireRootLayout()?.showShortSnackBar(
                    if (it) {
                        R.string.output_file_success
                    } else {
                        R.string.output_file_failed
                    }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CREATE_LOG_DOCUMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val outputUri = data?.data
                if (outputUri != null) {
                    requireSettingsViewModel()?.outputLogFileToUri(requireContext(), outputUri)
                } else {
                    requireRootLayout()?.showShortSnackBar(R.string.output_file_failed)
                }
            } else {
                requireSettingsViewModel()?.waitCreateLogFileName?.consume()
                requireRootLayout()?.showShortSnackBar(R.string.output_file_cancel)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDebugLogsSelectDialog(logs: Array<String>, @StringRes titleId: Int, onSelect: (String) -> Unit) {
        if (logs.isEmpty()) {
            requireRootLayout()?.showShortSnackBar(R.string.no_debug_logs)
        } else {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(titleId)
                setItems(logs) { _, i ->
                    onSelect(logs[i])
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
        }
    }
}