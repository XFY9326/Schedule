package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import lib.xfy9326.android.kit.PermissionCompat
import lib.xfy9326.android.kit.ScheduleNextAlarmPermissionContact
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.findPreference
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.IntentUtils

class AppWidgetSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.app_widget_settings
    override val preferenceResId: Int = R.xml.settings_app_widget

    @RequiresApi(Build.VERSION_CODES.S)
    private val requestScheduleNextAlarmPermission = registerForActivityResult(ScheduleNextAlarmPermissionContact()) {
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
        if (PermissionCompat.canScheduleNextAlarm(requireContext())) {
            R.string.permission_status_granted
        } else {
            R.string.permission_status_not_granted
        }
}