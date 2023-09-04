package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.bindPrefFragment

class ScheduleSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.schedule_settings
    override val preferenceResId: Int = R.xml.settings_schedule
    override val preferenceDataStore: PreferenceDataStore = ScheduleDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        bindPrefFragment<ScheduleCourseCellSettingsFragment>(R.string.pref_schedule_course_cell)
        bindPrefFragment<ScheduleTextSettingsFragment>(R.string.pref_schedule_text)
        bindPrefFragment<ScheduleBackgroundSettingsFragment>(R.string.pref_schedule_background)
        bindPrefFragment<ScheduleColorSettingsFragment>(R.string.pref_schedule_color)
        bindPrefFragment<ScheduleNotThisWeekSettingsFragment>(R.string.pref_schedule_not_this_week_style)
    }
}