package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.kt.findPreference
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.ui.dialog.NumberEditDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel

class ScheduleCourseCellSettingsFragment : AbstractSettingsFragment() {
    override val titleName: Int = R.string.schedule_course_cell_settings
    override val preferenceResId: Int = R.xml.settings_schedule_course_cell
    override val preferenceDataStore: PreferenceDataStore = ScheduleDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(R.string.pref_schedule_course_cell_specific_height) {
            showCourseCellHeightEditDialog(it.title.toString(), ScheduleDataStore.courseCellHeight.name)
        }
        setOnPrefClickListener(R.string.pref_schedule_course_cell_text_length) {
            showCourseCellTextLengthEditDialog(it.title.toString(), ScheduleDataStore.courseCellTextLength.name)
        }
        setOnPrefClickListener(R.string.pref_schedule_course_cell_course_text_length) {
            showCourseCellCourseTextLengthEditDialog(it.title.toString(), ScheduleDataStore.courseCellCourseTextLength.name)
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.courseCellHeight.observe(viewLifecycleOwner) {
            findPreference<Preference>(R.string.pref_schedule_course_cell_specific_height)?.summary =
                getString(R.string.schedule_course_cell_specific_height_sum, it)
        }
        viewModel.courseCellTextLength.observe(viewLifecycleOwner) {
            findPreference<Preference>(R.string.pref_schedule_course_cell_text_length)?.summary =
                getString(R.string.schedule_course_cell_text_length_sum, it)
        }
        viewModel.courseCellCourseTextLength.observe(viewLifecycleOwner) {
            findPreference<Preference>(R.string.pref_schedule_course_cell_course_text_length)?.summary =
                getString(R.string.schedule_course_cell_course_text_length_sum, it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NumberEditDialog.setOnNumberChangedListener(childFragmentManager, viewLifecycleOwner) { tag, number ->
            if (tag == ScheduleDataStore.courseCellHeight.name ||
                tag == ScheduleDataStore.courseCellTextLength.name ||
                tag == ScheduleDataStore.courseCellCourseTextLength.name
            ) {
                preferenceDataStore.putInt(tag, number)
            }
        }
    }

    private fun showCourseCellHeightEditDialog(title: String, tag: String) {
        val min = resources.getInteger(R.integer.min_schedule_course_cell_height)
        val max = resources.getInteger(R.integer.max_schedule_course_cell_height)
        lifecycleScope.launch {
            NumberEditDialog.showDialog(
                fragmentManager = childFragmentManager,
                tag = tag,
                number = ScheduleDataStore.courseCellHeightFlow.first(),
                minNumber = min,
                maxNumber = max,
                title = title,
                editHint = getString(R.string.schedule_course_cell_specific_height_hint, min, max)
            )
        }
    }

    private fun showCourseCellTextLengthEditDialog(title: String, tag: String) {
        val min = resources.getInteger(R.integer.min_schedule_course_cell_text_length)
        val max = resources.getInteger(R.integer.max_schedule_course_cell_text_length)
        lifecycleScope.launch {
            NumberEditDialog.showDialog(
                fragmentManager = childFragmentManager,
                tag = tag,
                number = ScheduleDataStore.courseCellTextLengthFlow.first(),
                minNumber = min,
                maxNumber = max,
                title = title,
                editHint = getString(R.string.input_text_length_hint, min, max)
            )
        }
    }

    private fun showCourseCellCourseTextLengthEditDialog(title: String, tag: String) {
        val min = resources.getInteger(R.integer.min_schedule_course_cell_course_text_length)
        val max = resources.getInteger(R.integer.max_schedule_course_cell_course_text_length)
        lifecycleScope.launch {
            NumberEditDialog.showDialog(
                fragmentManager = childFragmentManager,
                tag = tag,
                number = ScheduleDataStore.courseCellCourseTextLengthFlow.first(),
                minNumber = min,
                maxNumber = max,
                title = title,
                editHint = getString(R.string.input_text_length_hint, min, max)
            )
        }
    }
}