package tool.xfy9326.schedule.ui.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import io.github.xfy9326.atools.core.getParcelableCompat
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.getStringArray
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseDetail
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.databinding.DialogCourseDetailBinding
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.activity.CourseEditActivity
import tool.xfy9326.schedule.ui.adapter.CourseDetailAdapter
import tool.xfy9326.schedule.utils.setWindowPercent

class CourseDetailDialog : AppCompatDialogFragment() {
    companion object {
        private const val ARGUMENT_COURSE_DETAIL = "COURSE_DETAIL"
        private const val EXTRA_VIEW_LOAD_MODE = "BUNDLE_VIEW_LOAD_MODE"
        private const val WINDOW_WIDTH_PERCENT = 0.75

        fun showDialog(fragmentManager: FragmentManager, courseDetail: CourseDetail) {
            CourseDetailDialog().apply {
                arguments = bundleOf(
                    ARGUMENT_COURSE_DETAIL to courseDetail
                )
            }.show(fragmentManager, null)
        }
    }

    private lateinit var courseDetailAdapter: CourseDetailAdapter

    override fun getTheme(): Int {
        return R.style.AppTheme_MaterialRoundCornerDialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_VIEW_LOAD_MODE, courseDetailAdapter.isExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DialogCourseDetailBinding.inflate(layoutInflater, container, false).apply {
            val expandView = savedInstanceState?.getBoolean(EXTRA_VIEW_LOAD_MODE, false) ?: false
            val courseDetail = requireArguments().getParcelableCompat<CourseDetail>(ARGUMENT_COURSE_DETAIL)

            if (courseDetail != null) {
                initHeader(this, courseDetail.course)
                initDetailContent(this, expandView, courseDetail.scheduleTimes.times, courseDetail.course.times, courseDetail.currentTimeId)
            } else {
                dismissAllowingStateLoss()
            }
        }.root

    private fun initHeader(viewBinding: DialogCourseDetailBinding, course: Course) {
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

    private fun initDetailContent(
        viewBinding: DialogCourseDetailBinding,
        expandView: Boolean,
        scheduleTimes: List<ScheduleTime>,
        courseTimes: List<CourseTime>,
        currentTimeId: Long,
    ) {
        val weekDayStrArr = requireContext().getStringArray(R.array.weekday)

        courseDetailAdapter = CourseDetailAdapter(courseTimes, currentTimeId, scheduleTimes, weekDayStrArr, expandView)

        viewBinding.apply {
            recyclerViewCourseDetailContent.adapter = courseDetailAdapter

            val bottomButtonHeight = resources.getDimensionPixelSize(R.dimen.course_detail_dialog_bottom_button_height)
            val verticalPadding = if (courseDetailAdapter.timesSize > 1) 0 else bottomButtonHeight / 4
            recyclerViewCourseDetailContent.setPadding(0, verticalPadding, 0, verticalPadding)
        }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().setWindowPercent(WINDOW_WIDTH_PERCENT)
    }
}