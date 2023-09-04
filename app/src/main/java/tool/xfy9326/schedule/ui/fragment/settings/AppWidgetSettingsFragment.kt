package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import io.github.xfy9326.atools.core.ScheduleExactAlarmPermissionContract
import io.github.xfy9326.atools.core.canScheduleNextAlarm
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.findPreference
import tool.xfy9326.schedule.utils.setOnPrefClickListener

class AppWidgetSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.app_widget_settings
    override val preferenceResId: Int = R.xml.settings_app_widget

    @RequiresApi(Build.VERSION_CODES.S)
    private val requestScheduleNextAlarmPermission = registerForActivityResult(ScheduleExactAlarmPermissionContract()) {
        findPreference<Preference>(R.string.pref_schedule_next_alarm_permission)?.setSummary(getScheduleNextAlarmPreferenceSummaryResId())
    }

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(R.string.pref_auto_launch_permission) {
            IntentUtils.openAPPDetailSettings(requireContext())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findPreference<PreferenceCategory>(R.string.pref_accurate_app_widget_update)?.isVisible = true
            findPreference<Preference>(R.string.pref_schedule_next_alarm_permission)?.apply {
                setSummary(getScheduleNextAlarmPreferenceSummaryResId())
                setOnPreferenceClickListener {
                    requestScheduleNextAlarmPermission.launch(null)
                    false
                }
            }
        } else {
            findPreference<PreferenceCategory>(R.string.pref_accurate_app_widget_update)?.isVisible = false
        }
    }

    private fun getScheduleNextAlarmPreferenceSummaryResId() =
        if (requireContext().canScheduleNextAlarm()) {
            R.string.permission_status_granted
        } else {
            R.string.permission_status_not_granted
        }
}