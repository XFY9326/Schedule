package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.getRealScreenSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.ui.activity.module.ScheduleBackgroundModule
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.view.ScheduleViewHelper
import kotlin.math.max
import kotlin.math.min

class SchedulePreviewFragment : Fragment() {
    companion object {
        private const val TAG_SCHEDULE_VIEW = "ScheduleView"
        private const val TAG_SCHEDULE_BACKGROUND_IMAGE = "ScheduleBackgroundImage"
        private const val TAG_LAYOUT_SIZE_CONTAINER = "PreviewSizeContainer"
        private const val TAG_LAYOUT_CONTENT_CONTAINER = "PreviewContentContainer"
    }

    private val viewModel by activityViewModels<SettingsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return view ?: HorizontalScrollView(requireContext()).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = true
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            addView(FrameLayout(requireContext()).apply {
                tag = TAG_LAYOUT_SIZE_CONTAINER
                addView(
                    FrameLayout(requireContext()).apply {
                        tag = TAG_LAYOUT_CONTENT_CONTAINER
                        addView(
                            AppCompatImageView(requireContext()).also { it.tag = TAG_SCHEDULE_BACKGROUND_IMAGE },
                            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                                gravity = Gravity.CENTER
                            }
                        )
                    },
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                        gravity = Gravity.CENTER
                    }
                )
            }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.schedulePreviewStyles.observe(viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.Default) {
                val scheduleView = ScheduleViewHelper.buildPreviewScheduleView(requireContext(), it)
                updateScheduleView(scheduleView)
            }
        }
        viewModel.scheduleBackground.observe(viewLifecycleOwner) {
            requireView().findViewWithTag<AppCompatImageView>(TAG_SCHEDULE_BACKGROUND_IMAGE)?.let { v ->
                ScheduleBackgroundModule.setBackgroundView(v, it)
            }
        }
        viewModel.schedulePreviewPreviewWidth.observeEvent(viewLifecycleOwner) {
            updatePreviewViewWidth(it)
        }
    }

    private fun updatePreviewViewWidth(isLand: Boolean) {
        val newWidth = requireActivity().getRealScreenSize().let {
            if (isLand) max(it.first, it.second) else min(it.first, it.second)
        }
        requireView().findViewWithTag<ViewGroup>(TAG_LAYOUT_CONTENT_CONTAINER)?.updateLayoutParams<ViewGroup.LayoutParams> {
            width = newWidth
        }
    }

    private suspend fun updateScheduleView(view: View) = withContext(Dispatchers.Main.immediate) {
        requireView().findViewWithTag<ViewGroup>(TAG_LAYOUT_CONTENT_CONTAINER)?.apply {
            val scheduleView = findViewWithTag<View>(TAG_SCHEDULE_VIEW)
            if (view != scheduleView) {
                if (scheduleView != null) removeView(scheduleView)
                addView(view.also { it.tag = TAG_SCHEDULE_VIEW })
            }
        }
    }
}