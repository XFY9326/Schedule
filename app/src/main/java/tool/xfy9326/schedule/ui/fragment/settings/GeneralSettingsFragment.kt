package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.base.tryCast
import io.github.xfy9326.atools.base.tryEnumValueOf
import io.github.xfy9326.atools.ui.show
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.NightMode.Companion.modeInt
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.setOnPrefClickListener
import tool.xfy9326.schedule.utils.showSnackBar

class GeneralSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.general_settings
    override val preferenceResId: Int = R.xml.settings_general
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        findPreference<ListPreference>(AppSettingsDataStore.nightModeType.name)?.setOnPreferenceChangeListener { _, newValue ->
            newValue.tryCast<String>()?.let { tryEnumValueOf<NightMode>(it) }?.let(::changeNightMode)
            true
        }
        setOnPrefClickListener(R.string.pref_clear_cache) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.clear_cache)
                setMessage(R.string.clear_cache_msg)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    requireSettingsViewModel()?.clearCache()
                    requireRootLayout()?.showSnackBar(R.string.clear_cache_success)
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
        }
        setOnPrefClickListener(R.string.pref_restore_settings) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.restore_settings)
                setMessage(R.string.restore_settings_msg)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    restoreSettings()
                }
                setNegativeButton(android.R.string.cancel, null)
            }.show(viewLifecycleOwner)
        }
    }

    private fun changeNightMode(nightMode: NightMode) {
        val modeInt = nightMode.modeInt
        if (AppCompatDelegate.getDefaultNightMode() != modeInt) {
            requireActivity().window.setWindowAnimations(R.style.AppTheme_NightModeTransitionAnimation)
            AppCompatDelegate.setDefaultNightMode(modeInt)
        }
    }

    private fun restoreSettings() {
        requireSettingsViewModel()?.restoreSettings()
        requireActivity().onBackPressedDispatcher.onBackPressed()
        requireRootLayout()?.showSnackBar(R.string.restore_settings_success)
    }
}