package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.show
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.setOnPrefClickListener
import tool.xfy9326.schedule.utils.showSnackBar
import java.io.File

class DebugSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val TAG_READ_DEBUG_LOGS = "READ_DEBUG_LOGS"
        private const val TAG_OUTPUT_DEBUG_LOGS = "OUTPUT_DEBUG_LOGS"
        private const val TAG_SEND_DEBUG_LOG = "SEND_DEBUG_LOG"
    }

    override val titleName: Int = R.string.debug_settings
    override val preferenceResId: Int = R.xml.settings_debug
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)
    private val outputLogFile = registerForActivityResult(ActivityResultContracts.CreateDocument(MIMEConst.MIME_TEXT)) {
        if (it != null) {
            requireSettingsViewModel()?.outputLogFileToUri(it)
        } else {
            requireSettingsViewModel()?.waitCreateLogFileName?.consume()
            requireRootLayout()?.showSnackBar(R.string.output_file_cancel)
        }
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(R.string.pref_read_debug_logs) {
            requireSettingsViewModel()?.getAllLogs(TAG_READ_DEBUG_LOGS)
        }
        setOnPrefClickListener(R.string.pref_output_debug_logs) {
            requireSettingsViewModel()?.getAllLogs(TAG_OUTPUT_DEBUG_LOGS)
        }
        setOnPrefClickListener(R.string.pref_send_debug_log) {
            requireSettingsViewModel()?.getAllLogs(TAG_SEND_DEBUG_LOG)
        }
        setOnPrefClickListener(R.string.pref_clear_debug_logs) {
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

    override fun onBindLiveDataFromSettingsViewModel(viewModel: SettingsViewModel) {
        viewModel.allDebugLogs.observeEvent(viewLifecycleOwner) {
            when (it.first) {
                TAG_READ_DEBUG_LOGS -> showDebugLogsSelectDialog(it.second, R.string.read_debug_logs) { log ->
                    viewModel.showDebugLog(log)
                }

                TAG_OUTPUT_DEBUG_LOGS -> showDebugLogsSelectDialog(it.second, R.string.output_debug_logs) { log ->
                    viewModel.waitCreateLogFileName.write(log)
                    outputLogFile.launch(log.name)
                }

                TAG_SEND_DEBUG_LOG -> showDebugLogsSelectDialog(it.second, R.string.send_debug_log) { log ->
                    IntentUtils.sendCrashReport(requireContext(), log)
                }
            }
        }
        viewModel.showDebugLog.observeEvent(viewLifecycleOwner) {
            CrashViewDialog.showDialog(childFragmentManager, it)
        }
        viewModel.outputLogFileToUriResult.observeEvent(viewLifecycleOwner) {
            requireRootLayout()?.showSnackBar(
                if (it) R.string.output_file_success else R.string.output_file_failed
            )
        }
    }

    private fun showDebugLogsSelectDialog(logNames: List<File>, @StringRes titleId: Int, onSelect: (File) -> Unit) {
        if (logNames.isEmpty()) {
            requireRootLayout()?.showSnackBar(R.string.no_debug_logs)
        } else {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(titleId)
                setItems(logNames.map { it.nameWithoutExtension }.toTypedArray()) { _, i ->
                    onSelect(logNames[i])
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
        }
    }
}