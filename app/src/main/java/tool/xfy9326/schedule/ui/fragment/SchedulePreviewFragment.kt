package tool.xfy9326.schedule.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.getDrawableCompat
import io.github.xfy9326.atools.ui.getRealScreenSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.ui.activity.module.ScheduleBackgroundModule
import tool.xfy9326.schedule.ui.vm.SettingsViewModel
import tool.xfy9326.schedule.utils.getActionBarSize
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
        return view ?: ScrollView(requireContext()).apply {
            isVerticalScrollBarEnabled = false
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            HorizontalScrollView(requireContext()).apply {
                isHorizontalScrollBarEnabled = false
                isFillViewport = true
                overScrollMode = View.OVER_SCROLL_NEVER

                FrameLayout(requireContext()).apply {
                    tag = TAG_LAYOUT_SIZE_CONTAINER

                    FrameLayout(requireContext()).apply {
                        tag = TAG_LAYOUT_CONTENT_CONTAINER
                        background = requireContext().getDrawableCompat(R.drawable.background_schedule_preview)

                        addView(
                            AppCompatImageView(requireContext()).also { it.tag = TAG_SCHEDULE_BACKGROUND_IMAGE },
                            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                                gravity = Gravity.CENTER
                            }
                        )
                    }.also {
                        addView(it, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                            gravity = Gravity.CENTER
                        })
                    }
                }.also {
                    addView(it, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                }
            }.also {
                addView(it, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
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
        val (screenWidth, screenHeight) = requireActivity().getRealScreenSize()
        requireView().findViewWithTag<ViewGroup>(TAG_LAYOUT_CONTENT_CONTAINER)?.updateLayoutParams<ViewGroup.LayoutParams> {
            if (isLand) {
                width = max(screenWidth, screenHeight)
                height = min(screenWidth, screenHeight) - requireContext().getActionBarSize()
            } else {
                width = min(screenWidth, screenHeight)
                height = max(screenWidth, screenHeight) - requireContext().getActionBarSize()
            }
        }
    }

    private suspend fun updateScheduleView(view: View) = withContext(Dispatchers.Main.immediate) {
        requireView().findViewWithTag<ViewGroup>(TAG_LAYOUT_CONTENT_CONTAINER)?.apply {
            val scheduleView: View? = findViewWithTag(TAG_SCHEDULE_VIEW)
            if (view != scheduleView) {
                if (scheduleView != null) removeView(scheduleView)
                addView(view.also { it.tag = TAG_SCHEDULE_VIEW })
            }
        }
    }
}