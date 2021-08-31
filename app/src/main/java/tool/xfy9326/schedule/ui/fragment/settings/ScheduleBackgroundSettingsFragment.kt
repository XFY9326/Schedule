package tool.xfy9326.schedule.ui.fragment.settings

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import lib.xfy9326.android.kit.io.MIMEConst
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.kt.setOnPrefClickListener
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.dialog.FullScreenLoadingDialog
import tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel

class ScheduleBackgroundSettingsFragment : AbstractSettingsFragment() {
    private val loadingDialogController by lazy { FullScreenLoadingDialog.Controller.newInstance(viewLifecycleOwner, childFragmentManager) }
    private val selectBackgroundImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            loadingDialogController.show(false)
            requireSettingsViewModel()?.importScheduleImage(it)
        }
    }

    override val titleName: Int = R.string.schedule_background
    override val preferenceResId: Int = R.xml.settings_schedule_background
    override val preferenceDataStore: PreferenceDataStore = ScheduleDataStore.getPreferenceDataStore(lifecycleScope)

    override fun onPrefInit(savedInstanceState: Bundle?) {
        setOnPrefClickListener(R.string.pref_select_schedule_background_image) {
            selectBackgroundImage.launch(MIMEConst.MIME_IMAGE)
        }
    }

    override fun onBindLiveDataFromSettingsViewMode(viewModel: SettingsViewModel) {
        viewModel.importScheduleImage.observeEvent(viewLifecycleOwner) {
            loadingDialogController.hide()
            requireRootLayout()?.showSnackBar(if (it) R.string.schedule_background_set_success else R.string.schedule_background_set_failed)
        }
    }

    override fun getCallbackFragment(): Fragment {
        return requireParentFragment()
    }
}