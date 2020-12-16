package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.AboutActivity
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

class MainSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val ABOUT_PREFERENCE_KEY = "SETTINGS_ABOUT"
    }

    override val preferenceResId: Int = R.xml.settings_main
    override val titleName: Int = R.string.settings

    override fun onPrefInit(savedInstanceState: Bundle?) {
        findPreference<Preference>(ABOUT_PREFERENCE_KEY)?.setOnPreferenceClickListener {
            requireActivity().startActivity<AboutActivity>()
            false
        }
    }
}