package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleText
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.ui.dialog.ScheduleTextSizeEditDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.utils.setOnPrefClickListener
import tool.xfy9326.schedule.utils.showSnackBar

class ScheduleTextSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.schedule_text
    override val preferenceResId: Int = R.xml.settings_schedule_text
    override val preferenceDataStore: PreferenceDataStore = ScheduleDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        bindScheduleTextSizeDialog(R.string.pref_schedule_course_text_size, ScheduleText.COURSE_TEXT)
        bindScheduleTextSizeDialog(R.string.pref_schedule_time_text_size, ScheduleText.SCHEDULE_TIME_TEXT)
        bindScheduleTextSizeDialog(R.string.pref_schedule_number_text_size, ScheduleText.SCHEDULE_NUMBER_TEXT)
        bindScheduleTextSizeDialog(R.string.pref_schedule_header_month_text_size, ScheduleText.HEADER_MONTH_TEXT)
        bindScheduleTextSizeDialog(R.string.pref_schedule_header_month_date_text_size, ScheduleText.HEADER_MONTH_DATE_TEXT)
        bindScheduleTextSizeDialog(R.string.pref_schedule_header_weekday_text_size, ScheduleText.HEADER_WEEKDAY_TEXT)
        setOnPrefClickListener(R.string.pref_schedule_text_size_reset) {
            lifecycleScope.launch {
                ScheduleDataStore.resetScheduleTextSize()
                requireRootLayout()?.showSnackBar(R.string.reset_success)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ScheduleTextSizeEditDialog.setTextSizeEditListener(childFragmentManager, viewLifecycleOwner) { textType, size ->
            preferenceDataStore.putInt(textType.prefKey.name, size)
        }
    }

    private fun bindScheduleTextSizeDialog(@StringRes prefKeyRes: Int, scheduleText: ScheduleText) =
        setOnPrefClickListener(prefKeyRes) {
            lifecycleScope.launch {
                val textSize = ScheduleDataStore.readScheduleTextSize(scheduleText)
                ScheduleTextSizeEditDialog.showDialog(childFragmentManager, it.title.toString(), scheduleText, textSize)
            }
        }
}