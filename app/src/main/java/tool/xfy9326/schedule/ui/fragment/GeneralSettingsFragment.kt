package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.PreferenceDataStore
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.kt.tryEnumValueOf
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

@Suppress("unused")
class GeneralSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.general_settings
    override val preferenceResId: Int = R.xml.settings_general
    override val preferenceDataStore: PreferenceDataStore = AppSettingsDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        findPreference<ListPreference>(AppSettingsDataStore.nightModeType.name)?.setOnPreferenceChangeListener { _, newValue ->
            tryEnumValueOf<NightMode>(newValue as? String)?.let(::changeNightMode)
            true
        }
    }

    private fun changeNightMode(nightMode: NightMode) {
        val modeInt = nightMode.modeInt
        if (AppCompatDelegate.getDefaultNightMode() != modeInt) {
            requireActivity().window.setWindowAnimations(R.style.AppTheme_NightModeTransitionAnimation)
            AppCompatDelegate.setDefaultNightMode(modeInt)
        }
    }
}