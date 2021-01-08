package tool.xfy9326.schedule.ui.dialog

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.databinding.DialogCourseDetailBinding
import tool.xfy9326.schedule.databinding.ItemCourseDetailTimeBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.activity.CourseEditActivity
import kotlin.properties.Delegates

class CourseDetailDialog : DialogFragment() {
    companion object {
        private const val ARGUMENT_COURSE = "COURSE"
        private const val ARGUMENT_SCHEDULE_TIMES = "SCHEDULE_TIMES"
        private const val ARGUMENT_CURRENT_TIME_ID = "CURRENT_TIME_ID"
        private const val BUNDLE_VIEW_LOAD_MODE = "BUNDLE_VIEW_LOAD_MODE"
        private const val WINDOW_WIDTH_PERCENT = 0.75

        private val DIVIDER_HEIGHT = 1.dpToPx()
        private val DIVIDER_MARGIN = 12.dpToPx()

        fun showDialog(fragmentManager: FragmentManager, course: Course, scheduleTimes: Array<ScheduleTime>, currentTimeId: Long) {
            CourseDetailDialog().apply {
                arguments = buildBundle {
                    putSerializable(ARGUMENT_COURSE, course)
                    putSerializable(ARGUMENT_SCHEDULE_TIMES, scheduleTimes)
                    putLong(ARGUMENT_CURRENT_TIME_ID, currentTimeId)
                }
            }.show(fragmentManager, null)
        }
    }

    private var isViewExpanded by Delegates.notNull<Boolean>()

    private lateinit var course: Course
    private lateinit var scheduleTimes: Array<ScheduleTime>
    private var cellCourseTime: CourseTime? = null
    private lateinit var otherCourseTimes: Array<CourseTime>

    private lateinit var viewBinding: DialogCourseDetailBinding
    private var dividerBackground: Drawable? = null
    private lateinit var weekDayStrArr: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isViewExpanded = savedInstanceState?.getBoolean(BUNDLE_VIEW_LOAD_MODE, false) ?: false

        course = requireArguments().getSerializable(ARGUMENT_COURSE) as Course
        scheduleTimes = requireArguments().getSerializable(ARGUMENT_SCHEDULE_TIMES).castNonNull()

        val timeId = requireArguments().getLong(ARGUMENT_CURRENT_TIME_ID)

        val mutableList = course.times.toMutableList()
        val cellTime = mutableList.find {
            it.timeId == timeId
        }
        if (cellTime != null) {
            mutableList.remove(cellTime)
            otherCourseTimes = mutableList.toTypedArray()
        }
        cellCourseTime = cellTime
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_VIEW_LOAD_MODE, isViewExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        weekDayStrArr = context.getStringArray(R.array.weekday)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = DialogCourseDetailBinding.inflate(layoutInflater, container, false)
        setHeader()
        setDetailContent()
        if (isViewExpanded) loadModeCourseTimes()
        return viewBinding.root
    }

    private fun setHeader() {
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

    private fun setDetailContent() {
        viewBinding.apply {

            cellCourseTime?.let {
                layoutCourseDetailContent.addView(getCourseTimeView(it))
            }

            layoutCourseDetailContent.layoutTransition = getDetailContentLayoutTransition()

            if (otherCourseTimes.isEmpty()) {
                buttonExpand.isVisible = false
                val bottomPadding = resources.getDimensionPixelSize(R.dimen.course_detail_dialog_bottom_button_height) / 2
                layoutCourseDetailFrame.setPadding(0, 0, 0, bottomPadding)
            } else {
                layoutCourseDetailFrame.setPadding(0, 0, 0, 0)
            }

            buttonExpand.setOnClickListener {
                if (isViewExpanded) {
                    loadLessCourseTimes()
                } else {
                    loadModeCourseTimes()
                }
            }
        }
    }

    private fun getCourseTimeView(courseTime: CourseTime) =
        ItemCourseDetailTimeBinding.inflate(layoutInflater).apply {
            val weekNumText = courseTime.weekNumPattern.getText(requireContext())
            textViewCourseWeekNum.text =
                if (weekNumText.isEmpty()) {
                    getString(R.string.course_detail_week_num_simple, getString(R.string.undefined))
                } else {
                    getString(R.string.course_detail_week_num, weekNumText)
                }
            textViewCourseClassTime.text = getString(
                R.string.course_detail_class_time,
                getString(R.string.weekday, weekDayStrArr[courseTime.classTime.weekDay.ordinal]),
                courseTime.classTime.classTimeDescription(),
                courseTime.classTime.classTimeDescription(scheduleTimes)
            )
            val location = courseTime.location
            if (location == null) {
                textViewCourseLocation.isVisible = false
            } else {
                textViewCourseLocation.text = getString(R.string.course_detail_location, courseTime.location)
            }
        }.root

    private fun getDivider() = View(requireContext()).apply {
        layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, DIVIDER_HEIGHT).apply {
            setMargins(DIVIDER_MARGIN, 0, DIVIDER_MARGIN, 0)
            background = dividerBackground ?: requireContext().getDrawableCompat(R.color.dark_gray_icon).also {
                dividerBackground = it
            }
        }
    }

    private fun loadModeCourseTimes() {
        isViewExpanded = true
        viewBinding.apply {
            for (time in otherCourseTimes) {
                layoutCourseDetailContent.addView(getDivider())
                layoutCourseDetailContent.addView(getCourseTimeView(time))
            }
            buttonExpand.setImageResource(R.drawable.ic_expand_less_30)
        }
    }

    private fun loadLessCourseTimes() {
        isViewExpanded = false
        viewBinding.apply {
            if (cellCourseTime == null) {
                layoutCourseDetailContent.removeAllViewsInLayout()
            } else {
                layoutCourseDetailContent.removeViews(1, layoutCourseDetailContent.childCount - 1)
            }
            buttonExpand.setImageResource(R.drawable.ic_expand_more_30)
        }
    }

    private fun getDetailContentLayoutTransition() = LayoutTransition().apply {
        setAnimator(LayoutTransition.APPEARING, ValueAnimator.ofFloat(0f, 1f).apply {
            duration = resources.getInteger(R.integer.very_short_anim_time).toLong()
        })
        setAnimator(LayoutTransition.DISAPPEARING, null)
    }

    override fun onStart() {
        super.onStart()
        dialog?.setWindowWidthPercent(WINDOW_WIDTH_PERCENT)
    }
}