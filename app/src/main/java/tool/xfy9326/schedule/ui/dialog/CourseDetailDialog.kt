package tool.xfy9326.schedule.ui.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.databinding.DialogCourseDetailBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.activity.CourseEditActivity
import tool.xfy9326.schedule.ui.adapter.CourseDetailAdapter

class CourseDetailDialog : AppCompatDialogFragment() {
    companion object {
        private const val ARGUMENT_COURSE = "COURSE"
        private const val ARGUMENT_SCHEDULE_TIMES = "SCHEDULE_TIMES"
        private const val ARGUMENT_CURRENT_TIME_ID = "CURRENT_TIME_ID"
        private const val BUNDLE_VIEW_LOAD_MODE = "BUNDLE_VIEW_LOAD_MODE"
        private const val WINDOW_WIDTH_PERCENT = 0.75

        fun showDialog(fragmentManager: FragmentManager, course: Course, scheduleTimes: List<ScheduleTime>, currentTimeId: Long) {
            CourseDetailDialog().apply {
                arguments = buildBundle {
                    putSerializable(ARGUMENT_COURSE, course)
                    putSerializable(ARGUMENT_SCHEDULE_TIMES, scheduleTimes.toTypedArray())
                    putLong(ARGUMENT_CURRENT_TIME_ID, currentTimeId)
                }
            }.show(fragmentManager, null)
        }
    }

    private lateinit var course: Course
    private lateinit var courseDetailAdapter: CourseDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        course = requireArguments().getSerializable(ARGUMENT_COURSE) as Course
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_VIEW_LOAD_MODE, courseDetailAdapter.isExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DialogCourseDetailBinding.inflate(layoutInflater, container, false).apply {
            setHeader(this)
            setDetailContent(this, savedInstanceState?.getBoolean(BUNDLE_VIEW_LOAD_MODE, false) ?: false)
        }.root

    private fun setHeader(viewBinding: DialogCourseDetailBinding) {
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

    private fun setDetailContent(viewBinding: DialogCourseDetailBinding, expandView: Boolean) {
        val timeId = requireArguments().getLong(ARGUMENT_CURRENT_TIME_ID)
        val weekDayStrArr = requireContext().getStringArray(R.array.weekday)
        val scheduleTimes = requireArguments().getSerializable(ARGUMENT_SCHEDULE_TIMES).castNonNull<Array<ScheduleTime>>()

        courseDetailAdapter = CourseDetailAdapter(course.times, timeId, scheduleTimes, weekDayStrArr, expandView)

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
        dialog?.setWindowWidthPercent(WINDOW_WIDTH_PERCENT)
    }
}