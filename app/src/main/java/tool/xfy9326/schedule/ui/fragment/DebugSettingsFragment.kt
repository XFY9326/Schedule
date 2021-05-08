package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.show
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.IntentUtils

@Suppress("unused")
class DebugSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val KEY_READ_DEBUG_LOGS = "readDebugLogs"
        private const val KEY_OUTPUT_DEBUG_LOGS = "outputDebugLogs"
        private const val KEY_CLEAR_DEBUG_LOGS = "clearDebugLogs"
        private const val KEY_SEND_DEBUG_LOG = "sendDebugLog"
        private const val KEY_HANDLE_EXCEPTION = "handleException"
    }

    override val titleName: Int = R.string.debug_settings
    override val preferenceResId: Int = R.xml.settings_debug
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)
    private val outputLogFile = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            requireSettingsViewModel()?.outputLogFileToUri(it)
        } else {
            requireSettingsViewModel()?.waitCreateLogFileName?.consume()
            requireRootLayout()?.showSnackBar(R.string.output_file_cancel)
        }
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(KEY_READ_DEBUG_LOGS) {
            requireSettingsViewModel()?.getAllLogs(KEY_READ_DEBUG_LOGS)
        }
        setOnPrefClickListener(KEY_OUTPUT_DEBUG_LOGS) {
            requireSettingsViewModel()?.getAllLogs(KEY_OUTPUT_DEBUG_LOGS)
        }
        setOnPrefClickListener(KEY_SEND_DEBUG_LOG) {
            requireSettingsViewModel()?.getAllLogs(KEY_SEND_DEBUG_LOG)
        }
        setOnPrefClickListener(KEY_CLEAR_DEBUG_LOGS) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_debug_logs)
                setMessage(R.string.clear_debug_logs_msg)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearLogs()
                    requireRootLayout()?.showSnackBar(R.string.clear_debug_logs_success)
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.allDebugLogs.observeEvent(this) {
            when (it.first) {
                KEY_READ_DEBUG_LOGS -> showDebugLogsSelectDialog(it.second, R.string.read_debug_logs) { log ->
                    viewModel.showDebugLog(log)
                }
                KEY_OUTPUT_DEBUG_LOGS -> showDebugLogsSelectDialog(it.second, R.string.output_debug_logs) { log ->
                    viewModel.waitCreateLogFileName.write(log)
                    outputLogFile.launch(log)
                }
                KEY_SEND_DEBUG_LOG -> showDebugLogsSelectDialog(it.second, R.string.send_debug_log) { log ->
                    IntentUtils.sendCrashReport(requireContext(), log)
                }
            }
        }
        viewModel.showDebugLog.observeEvent(this) {
            CrashViewDialog.showDialog(childFragmentManager, it)
        }
        viewModel.outputLogFileToUriResult.observeEvent(this) {
            requireRootLayout()?.showSnackBar(
                if (it) {
                    R.string.output_file_success
                } else {
                    R.string.output_file_failed
                }
            )
        }
    }

    private fun showDebugLogsSelectDialog(logNames: List<String>, @StringRes titleId: Int, onSelect: (String) -> Unit) {
        if (logNames.isEmpty()) {
            requireRootLayout()?.showSnackBar(R.string.no_debug_logs)
        } else {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(titleId)
                setItems(logNames.map { it.substringBeforeLast(".") }.toTypedArray()) { _, i ->
                    onSelect(logNames[i])
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
        }
    }
}