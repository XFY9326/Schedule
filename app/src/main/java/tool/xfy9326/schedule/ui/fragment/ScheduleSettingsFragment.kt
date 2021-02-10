package tool.xfy9326.schedule.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceDataStore
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.data.base.DataStorePreferenceAdapter
import tool.xfy9326.schedule.kt.getColorCompat
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.IntentUtils

@Suppress("unused")
class ScheduleSettingsFragment : AbstractSettingsFragment() {
    companion object {
        private const val KEY_SELECT_SCHEDULE_BACKGROUND_IMAGE = "selectScheduleBackgroundImage"
        private const val REQUEST_CODE_SELECT_SCHEDULE_BACKGROUND_IMAGE = 1
    }

    private lateinit var loadingDialogController: FullScreenLoadingDialog.Controller

    override val titleName: Int = R.string.schedule_settings
    override val preferenceResId: Int = R.xml.settings_schedule
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
        loadingDialogController = FullScreenLoadingDialog.createControllerInstance(this)

        setOnPrefClickListener(KEY_SELECT_SCHEDULE_BACKGROUND_IMAGE) {
            startActivityForResult(IntentUtils.getSelectImageFromDocumentIntent(), REQUEST_CODE_SELECT_SCHEDULE_BACKGROUND_IMAGE)
        }
        findPreference<MultiSelectListPreference>(ScheduleDataStore.notThisWeekCourseShowStyle.name)?.setOnPreferenceChangeListener { _, newValue ->
            newValue as Set<*>
            if (newValue.isEmpty()) {
                requireRootLayout()?.showShortSnackBar(R.string.keep_at_least_one_style)
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
            requireRootLayout()?.showShortSnackBar(if (it) R.string.schedule_background_set_success else R.string.schedule_background_set_failed)
        }
    }

    // TODO: Deprecated
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT_SCHEDULE_BACKGROUND_IMAGE) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data
                if (uri == null) {
                    requireRootLayout()?.showShortSnackBar(R.string.image_select_failed)
                } else {
                    loadingDialogController.show(false)
                    requireSettingsViewModel()?.importScheduleImage(uri)
                }
            } else {
                requireRootLayout()?.showShortSnackBar(R.string.schedule_background_set_cancel)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}