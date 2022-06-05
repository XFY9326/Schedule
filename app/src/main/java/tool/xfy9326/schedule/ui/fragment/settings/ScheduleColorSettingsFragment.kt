package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import io.github.xfy9326.atools.datastore.preference.DataStorePreferenceAdapter
import io.github.xfy9326.atools.ui.getColorCompat
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

class ScheduleColorSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.schedule_color
    override val preferenceResId: Int = R.xml.settings_schedule_color
    override val preferenceDataStore: PreferenceDataStore = object : DataStorePreferenceAdapter(ScheduleDataStore.dataStore, lifecycleScope) {
        override fun getInt(key: String, defValue: Int): Int {
            when (key) {
                ScheduleDataStore.toolBarTintColor.name ->
                    return super.getInt(key, requireContext().getColorCompat(R.color.schedule_tool_bar_tint))
                ScheduleDataStore.timeTextColor.name ->
                    return super.getInt(key, requireContext().getColorCompat(R.color.course_time_cell_text))
                ScheduleDataStore.highlightShowTodayCellColor.name ->
                    return super.getInt(key, requireContext().getColorCompat(R.color.course_time_today_highlight))
            }
            return super.getInt(key, defValue)
        }
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        findPreference<ColorPreferenceCompat>(ScheduleDataStore.toolBarTintColor.name)?.presets = MaterialColorHelper.all()
        findPreference<ColorPreferenceCompat>(ScheduleDataStore.timeTextColor.name)?.presets = MaterialColorHelper.all()
    }
}