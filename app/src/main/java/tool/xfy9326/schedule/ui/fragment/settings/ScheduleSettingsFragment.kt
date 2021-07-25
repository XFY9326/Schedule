package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceDataStore
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.kt.bindPrefFragment
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

class ScheduleSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.schedule_settings
    override val preferenceResId: Int = R.xml.settings_schedule
    override val preferenceDataStore: PreferenceDataStore = ScheduleDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        bindPrefFragment<ScheduleTextSettingsFragment>(R.string.pref_schedule_text)
        bindPrefFragment<ScheduleBackgroundSettingsFragment>(R.string.pref_schedule_background)
        bindPrefFragment<ScheduleColorSettingsFragment>(R.string.pref_schedule_color)
        findPreference<MultiSelectListPreference>(ScheduleDataStore.notThisWeekCourseShowStyle.name)?.setOnPreferenceChangeListener { _, newValue ->
            newValue as Set<*>
            if (newValue.isEmpty()) {
                requireRootLayout()?.showSnackBar(R.string.keep_at_least_one_style)
                false
            } else {
                true
            }
        }
    }
}