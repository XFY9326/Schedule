package tool.xfy9326.schedule.ui.fragment

import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

@Suppress("unused")
class ScheduleSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.schedule_settings
    override val preferenceResId: Int = R.xml.settings_schedule
    override val preferenceDataStore: PreferenceDataStore = ScheduleDataStore.getPreferenceDataStore(lifecycleScope)
}