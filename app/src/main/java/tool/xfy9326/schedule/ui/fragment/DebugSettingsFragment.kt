package tool.xfy9326.schedule.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.observeEvent
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
    }

    override val titleName: Int = R.string.debug_settings
    override val preferenceResId: Int = R.xml.settings_debug
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(KEY_READ_DEBUG_LOGS)?.setOnPreferenceClickListener {
            requireSettingsViewModel()?.readDebugLogs()
            false
        }
        findPreference<Preference>(KEY_OUTPUT_DEBUG_LOGS)?.setOnPreferenceClickListener {
            requireSettingsViewModel()?.outputDebugLogs()
            false
        }
        findPreference<Preference>(KEY_CLEAR_DEBUG_LOGS)?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_debug_logs)
                setMessage(R.string.clear_debug_logs_msg)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearLogs()
                    requireRootLayout()?.showShortSnackBar(R.string.clear_debug_logs_success)
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
            false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireSettingsViewModel()?.readDebugLogs?.observeEvent(this) {
            showDebugLogsSelectDialog(it, R.string.read_debug_logs) { log ->
                requireSettingsViewModel()?.showDebugLog(log)
            }
        }
        requireSettingsViewModel()?.outputDebugLogs?.observeEvent(this) {
            showDebugLogsSelectDialog(it, R.string.output_debug_logs) { log ->
                requireContext().startActivity(IntentUtils.getShareLogIntent(requireContext(), log))
            }
        }
        requireSettingsViewModel()?.showDebugLog?.observeEvent(this) {
            CrashViewDialog.showDialog(childFragmentManager, it)
        }
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