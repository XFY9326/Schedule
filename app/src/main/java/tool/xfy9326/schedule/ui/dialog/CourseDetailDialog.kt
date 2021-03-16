package tool.xfy9326.schedule.ui.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.databinding.DialogCourseDetailBinding
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.getColorCompat
import tool.xfy9326.schedule.kt.getStringArray
import tool.xfy9326.schedule.kt.setWindowWidthPercent
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.activity.CourseEditActivity
import tool.xfy9326.schedule.ui.adapter.CourseDetailAdapter
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils

class CourseDetailDialog : AppCompatDialogFragment() {
    companion object {
        private const val ARGUMENT_COURSE_ID = "COURSE_ID"
        private const val ARGUMENT_TIME_ID = "TIME_ID"
        private const val BUNDLE_VIEW_LOAD_MODE = "BUNDLE_VIEW_LOAD_MODE"
        private const val WINDOW_WIDTH_PERCENT = 0.75

        fun showDialog(fragmentManager: FragmentManager, courseId: Long, timeId: Long) {
            CourseDetailDialog().apply {
                arguments = bundleOf(
                    ARGUMENT_COURSE_ID to courseId,
                    ARGUMENT_TIME_ID to timeId
                )
            }.show(fragmentManager, null)
        }
    }

    private lateinit var viewBinding: DialogCourseDetailBinding
    private lateinit var courseDetailAdapter: CourseDetailAdapter

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_VIEW_LOAD_MODE, courseDetailAdapter.isExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DialogCourseDetailBinding.inflate(layoutInflater, container, false).apply {
            viewBinding = this
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val courseId = requireArguments().getLong(ARGUMENT_COURSE_ID)
        val timeId = requireArguments().getLong(ARGUMENT_TIME_ID)
        val expandView = savedInstanceState?.getBoolean(BUNDLE_VIEW_LOAD_MODE, false) ?: false
        viewLifecycleOwner.lifecycleScope.launch {
            val result = loadScheduleCourseData(courseId)
            if (result != null) {
                initHeader(result.second)
                initDetailContent(expandView, result.first, result.second.times, timeId)
            } else {
                dismissAllowingStateLoss()
            }
        }
    }

    private fun initHeader(course: Course) {
        viewBinding.apply {
            layoutCourseHeader.setBackgroundColor(course.color)

            val textColor = if (MaterialColorHelper.isLightColor(course.color)) {
                requireContext().getColorCompat(R.color.course_cell_text_dark)
            } else {
                requireContext().getColorCompat(R.color.course_cell_text_light)
            }
            textViewCourseName.setTextColor(textColor)
            textViewTeacherName.setTextColor(textColor)

            textViewCourseName.text = course.name
            val teacher = course.teacher
            if (teacher == null) {
                textViewTeacherName.isVisible = false
            } else {
                textViewTeacherName.text = course.teacher
            }

            buttonCourseEdit.imageTintList = ColorStateList.valueOf(textColor)
            buttonCourseEdit.setOnClickListener {
                requireContext().startActivity<CourseEditActivity> {
                    putExtra(CourseEditActivity.INTENT_EXTRA_COURSE_ID, course.courseId)
                    putExtra(CourseEditActivity.INTENT_EXTRA_SCHEDULE_ID, course.scheduleId)
                }
                dismiss()
            }
        }
    }

    private fun initDetailContent(expandView: Boolean, scheduleTimes: List<ScheduleTime>, courseTimes: List<CourseTime>, currentTimeId: Long) {
        val weekDayStrArr = requireContext().getStringArray(R.array.weekday)

        courseDetailAdapter = CourseDetailAdapter(courseTimes, currentTimeId, scheduleTimes, weekDayStrArr, expandView)

        viewBinding.apply {
            recyclerViewCourseDetailContent.adapter = courseDetailAdapter

            val verticalPadding = if (courseDetailAdapter.timesSize > 1) {
                0
            } else {
                resources.getDimensionPixelSize(R.dimen.course_detail_dialog_bottom_button_height) / 4
            }
            recyclerViewCourseDetailContent.setPadding(0, verticalPadding, 0, verticalPadding)
        }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().setWindowWidthPercent(WINDOW_WIDTH_PERCENT)
    }

    private suspend fun loadScheduleCourseData(courseId: Long) = withContext(Dispatchers.IO) {
        val course = ScheduleDBProvider.db.scheduleDAO.getScheduleCourse(courseId).firstOrNull()
        if (course != null) {
            ScheduleUtils.currentScheduleFlow.first().times to course
        } else {
            null
        }
    }
}