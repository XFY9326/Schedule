package tool.xfy9326.schedule.ui.fragment

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.tools.JobQueue
import tool.xfy9326.schedule.ui.view.ScheduleView
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.CourseManager
import kotlin.properties.Delegates

class TableFragment : Fragment() {
    companion object {
        private const val ARGUMENT_WEEK_NUM = "ARGUMENT_WEEK_NUM"

        fun create(weekNum: Int) = TableFragment().apply {
            arguments = buildBundle {
                putInt(ARGUMENT_WEEK_NUM, weekNum)
            }
        }
    }

    private val viewCreatedJobQueue = JobQueue(lifecycleScope.coroutineContext)
    private var weekNum by Delegates.notNull<Int>()
    private lateinit var viewModel: ScheduleViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        weekNum = requireArguments().getInt(ARGUMENT_WEEK_NUM)
        viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]

        viewModel.scheduleBuildData.observe(this) {
            lifecycleScope.launch(Dispatchers.Default) {
                val courses = CourseManager.getScheduleViewDataByWeek(weekNum, it.first, it.second, it.third.showNotThisWeekCourse)
                val scheduleView = ScheduleView(requireContext(), courses, it.third)
                scheduleView.setOnCourseClickListener(this@TableFragment::onCourseCellClick)
                viewCreatedJobQueue.submit {
                    updateScheduleView(scheduleView)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return view ?: FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutTransition = LayoutTransition().apply {
                setAnimateParentHierarchy(false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCreatedJobQueue.allowRunning()
    }

    override fun onDestroyView() {
        viewCreatedJobQueue.cancelAll()
        super.onDestroyView()
    }

    private suspend fun updateScheduleView(scheduleView: ScheduleView) = withContext(Dispatchers.Main) {
        (requireView() as ViewGroup).apply {
            if (childCount > 0) removeAllViewsInLayout()
            addView(scheduleView)
        }
    }

    private fun onCourseCellClick(courseCell: CourseCell) {
        viewModel.showCourseDetailDialog(courseCell.courseId, courseCell.timeId)
    }
}