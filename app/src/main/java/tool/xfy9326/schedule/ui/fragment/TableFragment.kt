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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.ui.view.ScheduleView
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
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

    private var weekNum by Delegates.notNull<Int>()
    private lateinit var viewModel: ScheduleViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        weekNum = requireArguments().getInt(ARGUMENT_WEEK_NUM)
        viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]
        viewModel.getScheduleBuildBundleLiveData(weekNum).observe(this, ::onBuildView)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return view ?: FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutTransition = LayoutTransition().apply {
                setAnimateParentHierarchy(false)
            }
        }
    }

    private fun onBuildView(bundle: ScheduleBuildBundle) {
        lifecycleScope.launch(Dispatchers.Default) {
            val scheduleView = ScheduleView(requireContext(), bundle)
            scheduleView.setOnCourseClickListener(this@TableFragment::onCourseCellClick)
            lifecycleScope.launchWhenStarted {
                (requireView() as ViewGroup).apply {
                    if (childCount > 0) removeAllViewsInLayout()
                    addView(scheduleView)
                }
            }
        }
    }

    private fun onCourseCellClick(courseCell: CourseCell) {
        viewModel.showCourseDetailDialog(courseCell.courseId, courseCell.timeId)
    }
}