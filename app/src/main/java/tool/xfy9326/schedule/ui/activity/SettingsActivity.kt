package tool.xfy9326.schedule.ui.activity

import androidx.activity.viewModels
import tool.xfy9326.schedule.ui.activity.base.AbstractSettingsActivity
import tool.xfy9326.schedule.ui.fragment.settings.MainSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel

class SettingsActivity : AbstractSettingsActivity() {
    val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreateMainSettingsFragment() = MainSettingsFragment()
}