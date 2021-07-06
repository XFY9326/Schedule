package tool.xfy9326.schedule.ui.fragment.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.Sp
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.dialog.TextSizeEditDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

class ScheduleTextSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private fun getDefaultTextSizeEditParams(context: Context, @Sp value: Int, title: String) =
            TextSizeEditDialog.Params(
                value,
                context.resources.getInteger(R.integer.schedule_text_size_offset),
                context.resources.getInteger(R.integer.schedule_text_size_min),
                context.resources.getInteger(R.integer.schedule_text_size_max),
                title
            )
    }

    override val titleName: Int = R.string.schedule_text_settings
    override val preferenceResId: Int = R.xml.settings_schedule_text
    override val preferenceDataStore: PreferenceDataStore = ScheduleDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(R.string.pref_schedule_course_text_size) {
            editTextSize(ScheduleDataStore.courseTextSize, resources.getInteger(R.integer.schedule_course_default_text_size), it.title.toString())
        }
        setOnPrefClickListener(R.string.pref_schedule_time_text_size) {
            editTextSize(ScheduleDataStore.scheduleTimeTextSize, resources.getInteger(R.integer.schedule_time_default_text_size), it.title.toString())
        }
        setOnPrefClickListener(R.string.pref_schedule_number_text_size) {
            editTextSize(ScheduleDataStore.scheduleNumberTextSize, resources.getInteger(R.integer.schedule_number_default_text_size), it.title.toString())
        }
        setOnPrefClickListener(R.string.pref_schedule_header_month_text_size) {
            editTextSize(ScheduleDataStore.headerMonthTextSize, resources.getInteger(R.integer.schedule_header_month_default_text_size), it.title.toString())
        }
        setOnPrefClickListener(R.string.pref_schedule_header_month_date_text_size) {
            editTextSize(ScheduleDataStore.headerMonthDateTextSize, resources.getInteger(R.integer.schedule_header_month_date_default_text_size), it.title.toString())
        }
        setOnPrefClickListener(R.string.pref_schedule_header_weekday_text_size) {
            editTextSize(ScheduleDataStore.headerWeekDayTextSize, resources.getInteger(R.integer.schedule_header_weekday_default_text_size), it.title.toString())
        }
        setOnPrefClickListener(R.string.pref_schedule_text_size_reset) {
            lifecycleScope.launch {
                ScheduleDataStore.resetScheduleTextSize()
                requireRootLayout()?.showSnackBar(R.string.reset_success)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TextSizeEditDialog.setTextSizeEditListener(childFragmentManager, viewLifecycleOwner) { key, size ->
            preferenceDataStore.putInt(key, size)
        }
    }

    private fun editTextSize(key: Preferences.Key<Int>, defaultValue: Int, title: String) {
        lifecycleScope.launch {
            val value = preferenceDataStore.getInt(key.name, defaultValue)
            TextSizeEditDialog.showDialog(childFragmentManager, key.name, getDefaultTextSizeEditParams(requireContext(), value, title))
        }
    }
}