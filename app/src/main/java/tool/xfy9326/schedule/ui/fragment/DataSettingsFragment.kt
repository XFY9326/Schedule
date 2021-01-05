package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.show
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

@Suppress("unused")
class DataSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val KEY_CLEAR_CACHE = "clearCache"
        private const val KEY_RESTORE_SETTINGS = "restoreSettings"
    }

    override val titleName: Int = R.string.data_settings
    override val preferenceResId: Int = R.xml.settings_data
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(KEY_CLEAR_CACHE)?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_cache)
                setMessage(R.string.clear_cache_msg)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearCache(requireContext())
                    requireRootLayout()?.showShortSnackBar(R.string.clear_cache_success)
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
            false
        }
        findPreference<Preference>(KEY_RESTORE_SETTINGS)?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.restore_settings)
                setMessage(R.string.restore_settings_msg)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    restoreSettings()
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
            false
        }
    }

    private fun restoreSettings() {
        requireSettingsViewModel()?.restoreSettings()
        // Restore current page settings
        findPreference<CheckBoxPreference>(AppSettingsDataStore.keepWebProviderCache.name)?.isChecked = false

        requireRootLayout()?.showShortSnackBar(R.string.restore_settings_success)
    }
}