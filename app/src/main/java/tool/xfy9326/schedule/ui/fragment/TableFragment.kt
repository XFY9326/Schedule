package tool.xfy9326.schedule.ui.fragment

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.ui.view.ScheduleView
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.CourseManager
import kotlin.properties.Delegates

class TableFragment : Fragment(), Observer<Triple<Schedule, Array<Course>, ScheduleStyles>> {
    companion object {
        private const val ARGUMENT_WEEK_NUM = "ARGUMENT_WEEK_NUM"

        fun create(weekNum: Int) = TableFragment().apply {
            arguments = buildBundle {
                putInt(ARGUMENT_WEEK_NUM, weekNum)
            }
        }
    }

    private val viewModel by activityViewModels<ScheduleViewModel>()
    private var weekNum by Delegates.notNull<Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        weekNum = requireArguments().getInt(ARGUMENT_WEEK_NUM)

        viewModel.scheduleBuildData.observe(this, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return view ?: FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutTransition = LayoutTransition().apply {
                setAnimateParentHierarchy(false)
            }
        }
    }

    override fun onChanged(data: Triple<Schedule, Array<Course>, ScheduleStyles>) {
        context?.let {
            lifecycleScope.launch(Dispatchers.Default) {
                val scheduleData = CourseManager.getScheduleViewDataByWeek(weekNum, data.first, data.second, data.third)
                val scheduleView = ScheduleView(it, scheduleData)
                scheduleView.setOnCourseClickListener(this@TableFragment::onCourseCellClick)
                lifecycleScope.launchWhenStarted {
                    updateScheduleView(scheduleView)
                }
            }
        }
    }

    private fun updateScheduleView(scheduleView: ScheduleView) {
        (requireView() as ViewGroup).apply {
            if (childCount > 0) removeAllViewsInLayout()
            addView(scheduleView)
        }
    }

    private fun onCourseCellClick(courseCell: CourseCell) {
        viewModel.showCourseDetailDialog(courseCell.courseId, courseCell.timeId)
    }
}