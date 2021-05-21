package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceDataStore
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.data.base.DataStorePreferenceAdapter
import tool.xfy9326.schedule.kt.getColorCompat
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel

@Suppress("unused")
class ScheduleSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val KEY_SELECT_SCHEDULE_BACKGROUND_IMAGE = "selectScheduleBackgroundImage"
    }

    private val loadingDialogController by lazy { FullScreenLoadingDialog.createControllerInstance(viewLifecycleOwner, childFragmentManager) }
    private val selectBackgroundImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) {
            requireRootLayout()?.showSnackBar(R.string.schedule_background_set_cancel)
        } else {
            loadingDialogController.show(false)
            requireSettingsViewModel()?.importScheduleImage(it)
        }
    }

    override val titleName: Int = R.string.schedule_settings
    override val preferenceResId: Int = R.xml.settings_schedule
    override val preferenceDataStore: PreferenceDataStore = object : DataStorePreferenceAdapter(ScheduleDataStore, lifecycleScope) {
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
        setOnPrefClickListener(KEY_SELECT_SCHEDULE_BACKGROUND_IMAGE) {
            selectBackgroundImage.launch(MIMEConst.MIME_IMAGE)
        }
        findPreference<MultiSelectListPreference>(ScheduleDataStore.notThisWeekCourseShowStyle.name)?.setOnPreferenceChangeListener { _, newValue ->
            newValue as Set<*>
            if (newValue.isEmpty()) {
                requireRootLayout()?.showSnackBar(R.string.keep_at_least_one_style)
                false
            } else {
                true
            }
        }
        findPreference<ColorPreferenceCompat>(ScheduleDataStore.toolBarTintColor.name)?.presets = MaterialColorHelper.all()
        findPreference<ColorPreferenceCompat>(ScheduleDataStore.timeTextColor.name)?.presets = MaterialColorHelper.all()
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.importScheduleImage.observeEvent(this) {
            loadingDialogController.hide()
            requireRootLayout()?.showSnackBar(if (it) R.string.schedule_background_set_success else R.string.schedule_background_set_failed)
        }
    }
}