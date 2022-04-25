package tool.xfy9326.schedule.ui.fragment.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.xfy9326.atools.livedata.postEvent
import io.github.xfy9326.atools.ui.getRealScreenSize
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.FragmentScheduleSettingsBinding
import tool.xfy9326.schedule.kt.getDefaultBackgroundColor
import tool.xfy9326.schedule.ui.base.OnRequestBackCallback
import tool.xfy9326.schedule.ui.fragment.SchedulePreviewFragment
import tool.xfy9326.schedule.ui.fragment.base.ViewBindingFragment
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.view.ViewUtils
import kotlin.properties.Delegates

class ScheduleBaseSettingsFragment : ViewBindingFragment<FragmentScheduleSettingsBinding>(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, OnRequestBackCallback {
    companion object {
        private const val EXTRA_IS_PREVIEW_LAND_MODE = "EXTRA_IS_PREVIEW_LAND_MODE"
    }

    private val viewModel by activityViewModels<SettingsViewModel>()
    private var isCurrentPreviewLandMode by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentScheduleSettingsBinding.inflate(inflater, container, false)

    override fun onBindViewBinding(view: View) = FragmentScheduleSettingsBinding.bind(view)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.also {
            it.setBackgroundColor(requireContext().getDefaultBackgroundColor())
        }
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: FragmentScheduleSettingsBinding) {
        if (bundle == null) {
            childFragmentManager.commit {
                replace(R.id.container_fragmentPreview, SchedulePreviewFragment())
                replace(R.id.container_fragmentSettings, ScheduleSettingsFragment())
            }
        }
        isCurrentPreviewLandMode = bundle?.getBoolean(EXTRA_IS_PREVIEW_LAND_MODE, false) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requireViewBinding().containerFragmentPreview.updateLayoutParams<LinearLayoutCompat.LayoutParams> {
                width = requireActivity().getRealScreenSize().second
            }
        }
        super.onViewCreated(view, savedInstanceState)
        viewModel.schedulePreviewPreviewWidth.postEvent(isCurrentPreviewLandMode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_PREVIEW_LAND_MODE, isCurrentPreviewLandMode)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_schedule_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_schedulePreviewRatio) {
            isCurrentPreviewLandMode = !isCurrentPreviewLandMode
            viewModel.schedulePreviewPreviewWidth.postEvent(isCurrentPreviewLandMode)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        return ViewUtils.navigatePreferenceFragmentWithAnimation(requireContext(), childFragmentManager, R.id.container_fragmentSettings, pref)
    }

    override fun onRequestBack(): Boolean {
        return if (childFragmentManager.backStackEntryCount == 0) {
            false
        } else {
            childFragmentManager.popBackStack()
            true
        }
    }
}