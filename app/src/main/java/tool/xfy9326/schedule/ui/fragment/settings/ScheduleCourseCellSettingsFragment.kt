package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.Flow
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

    private val numberTextEditArray = arrayOf(
        NumberTextEditBundle(
            R.string.pref_schedule_course_cell_vertical_padding,
            R.string.schedule_course_cell_padding_sum,
            R.integer.min_schedule_course_cell_vertical_padding,
            R.integer.max_schedule_course_cell_vertical_padding,
            ScheduleDataStore.courseCellVerticalPadding,
            ScheduleDataStore.courseCellVerticalPaddingFlow,
            R.string.schedule_course_cell_padding_hint
        ),
        NumberTextEditBundle(
            R.string.pref_schedule_course_cell_horizontal_padding,
            R.string.schedule_course_cell_padding_sum,
            R.integer.min_schedule_course_cell_horizontal_padding,
            R.integer.max_schedule_course_cell_horizontal_padding,
            ScheduleDataStore.courseCellHorizontalPadding,
            ScheduleDataStore.courseCellHorizontalPaddingFlow,
            R.string.schedule_course_cell_padding_hint
        ),
        NumberTextEditBundle(
            R.string.pref_schedule_course_cell_specific_height,
            R.string.schedule_course_cell_specific_height_sum,
            R.integer.min_schedule_course_cell_height,
            R.integer.max_schedule_course_cell_height,
            ScheduleDataStore.courseCellHeight,
            ScheduleDataStore.courseCellHeightFlow,
            R.string.schedule_course_cell_specific_height_hint
        ),
        NumberTextEditBundle(
            R.string.pref_schedule_course_cell_text_length,
            R.string.schedule_course_cell_text_length_sum,
            R.integer.min_schedule_course_cell_text_length,
            R.integer.max_schedule_course_cell_text_length,
            ScheduleDataStore.courseCellTextLength,
            ScheduleDataStore.courseCellTextLengthFlow,
            R.string.input_text_length_hint
        ),
        NumberTextEditBundle(
            R.string.pref_schedule_course_cell_course_text_length,
            R.string.schedule_course_cell_course_text_length_sum,
            R.integer.min_schedule_course_cell_course_text_length,
            R.integer.max_schedule_course_cell_course_text_length,
            ScheduleDataStore.courseCellCourseTextLength,
            ScheduleDataStore.courseCellCourseTextLengthFlow,
            R.string.input_text_length_hint
        )
    )

    override fun onPrefInit(savedInstanceState: Bundle?) {
        for (bundle in numberTextEditArray) {
            val min = resources.getInteger(bundle.minRes)
            val max = resources.getInteger(bundle.maxRes)
            setOnPrefClickListener(bundle.prefKeyRes) {
                lifecycleScope.launch {
                    NumberEditDialog.showDialog(
                        fragmentManager = childFragmentManager,
                        tag = bundle.dataStoreKey.name,
                        number = bundle.dataStoreFlow.first(),
                        minNumber = min,
                        maxNumber = max,
                        title = it.title.toString(),
                        editHint = getString(bundle.editHintRes, min, max)
                    )
                }
            }
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        for (bundle in numberTextEditArray) {
            viewLifecycleOwner.lifecycleScope.launch {
                bundle.dataStoreFlow.collect {
                    findPreference<Preference>(bundle.prefKeyRes)?.summary =
                        getString(bundle.prefSumRes, it)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NumberEditDialog.setOnNumberChangedListener(childFragmentManager, viewLifecycleOwner) { tag, number ->
            numberTextEditArray.find { it.dataStoreKey.name == tag }?.let {
                preferenceDataStore.putInt(it.dataStoreKey.name, number)
            }
        }
    }

    class NumberTextEditBundle(
        @StringRes
        val prefKeyRes: Int,
        @StringRes
        val prefSumRes: Int,
        @IntegerRes
        val minRes: Int,
        @IntegerRes
        val maxRes: Int,
        val dataStoreKey: Preferences.Key<Int>,
        val dataStoreFlow: Flow<Int>,
        @StringRes
        val editHintRes: Int,
    )
}