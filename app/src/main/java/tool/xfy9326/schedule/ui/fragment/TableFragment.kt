package tool.xfy9326.schedule.ui.fragment

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.schedule.CourseUtils
import tool.xfy9326.schedule.utils.view.ScheduleViewHelper
import kotlin.properties.Delegates

class TableFragment : Fragment(), Observer<ScheduleBuildBundle> {
    companion object {
        private const val ARGUMENT_WEEK_NUM = "ARGUMENT_WEEK_NUM"

        fun create(weekNum: Int) = TableFragment().apply {
            arguments = bundleOf(
                ARGUMENT_WEEK_NUM to weekNum
            )
        }
    }

    private val viewModel by activityViewModels<ScheduleViewModel>()
    private var weekNum by Delegates.notNull<Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        weekNum = requireArguments().getInt(ARGUMENT_WEEK_NUM)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return view ?: FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutTransition = LayoutTransition().apply { setAnimateParentHierarchy(false) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.scheduleBuildData.observeForever(this)
    }

    override fun onChanged(value: ScheduleBuildBundle) {
        lifecycleScope.launch(Dispatchers.Default) {
            val viewData = CourseUtils.getScheduleViewDataByWeek(weekNum, value)
            val scheduleView = ScheduleViewHelper.buildScheduleView(requireContext(), viewData, ::onCourseCellClick)
            updateScheduleView(scheduleView)
        }
    }

    override fun onDestroyView() {
        viewModel.scheduleBuildData.removeObserver(this)
        super.onDestroyView()
    }

    private suspend fun updateScheduleView(view: View) = withContext(Dispatchers.Main.immediate) {
        (requireView() as? ViewGroup)?.apply {
            if (view != children.firstOrNull()) {
                removeAllViewsInLayout()
                addView(view)
            }
        }
    }

    private fun onCourseCellClick(courseCell: CourseCell) {
        viewModel.showCourseDetailDialog(courseCell.courseId, courseCell.timeId)
    }
}