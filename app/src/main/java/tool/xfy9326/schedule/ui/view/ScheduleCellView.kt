package tool.xfy9326.schedule.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.kt.NEW_LINE
import tool.xfy9326.schedule.tools.MaterialColorHelper
import kotlin.properties.Delegates


@SuppressLint("ViewConstructor")
class ScheduleCellView : LinearLayoutCompat {
    private val predefine: SchedulePredefine
    private val styles: ScheduleStyles
    private var courseCellClickListener: ((CourseCell) -> Unit)? = null

    var column by Delegates.notNull<Int>()
        private set

    var row by Delegates.notNull<Int>()
        private set

    var rowSpan by Delegates.notNull<Int>()
        private set

    constructor(
        context: Context,
        showWeekend: Boolean,
        courseCell: CourseCell,
        schedulePredefine: SchedulePredefine,
        scheduleSettings: ScheduleStyles,
        weekStart: WeekDay,
        courseCellClickListener: ((CourseCell) -> Unit),
    ) : this(context, schedulePredefine, scheduleSettings) {
        if (showWeekend || weekStart == WeekDay.MONDAY) {
            this.column = courseCell.classTime.weekDay.orderedValue(weekStart)
        } else {
            this.column = courseCell.classTime.weekDay.orderedValue(weekStart) - 1
        }
        this.row = courseCell.classTime.classStartTime - 1
        this.rowSpan = courseCell.classTime.classDuration
        this.courseCellClickListener = courseCellClickListener

        initAsCourseCell(courseCell)
    }

    constructor(
        context: Context,
        index: Int,
        scheduleTime: ScheduleTime,
        schedulePredefine: SchedulePredefine,
        scheduleSettings: ScheduleStyles,
    ) : this(context, schedulePredefine, scheduleSettings) {
        this.column = 0
        this.row = index
        this.rowSpan = 1

        initAsScheduleTimeCell(index, scheduleTime)
    }

    private constructor(
        context: Context,
        schedulePredefine: SchedulePredefine,
        scheduleSettings: ScheduleStyles,
    ) : super(context) {
        this.predefine = schedulePredefine
        this.styles = scheduleSettings
        initStyle()
    }

    private fun initStyle() {
        alpha = styles.scheduleViewAlpha
        orientation = VERTICAL
        setPadding(predefine.gridCellPadding)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initAsCourseCell(courseCell: CourseCell) {
        TextView(context).apply {
            val showText = buildString {
                if (courseCell.courseLocation == null) {
                    append(courseCell.courseName)
                } else {
                    append(context.getString(R.string.course_cell_text, courseCell.courseName, courseCell.courseLocation))
                }
            }
            text = if (!courseCell.isThisWeekCourse && NotThisWeekCourseShowStyle.SHOW_NOT_THIS_WEEK_TEXT in styles.notThisWeekCourseShowStyle) {
                val notThisWeekText = context.getString(R.string.not_this_week)
                SpannableStringBuilder(notThisWeekText + showText).apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, notThisWeekText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            } else {
                showText
            }
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            background = ScheduleView.buildBackground(courseCell.cellColor, predefine.courseCellRippleColor, predefine.courseCellBackgroundRadius)

            setTextSize(TypedValue.COMPLEX_UNIT_PX, styles.getCourseCellTextSize(context))
            setPadding(predefine.courseCellTextPadding)

            setTextColor(
                if (MaterialColorHelper.isLightColor(courseCell.cellColor)) {
                    predefine.courseCellTextColorDark
                } else {
                    predefine.courseCellTextColorLight
                }
            )

            if (!courseCell.isThisWeekCourse && NotThisWeekCourseShowStyle.USE_TRANSPARENT_BACKGROUND in styles.notThisWeekCourseShowStyle) {
                alpha = predefine.notThisWeekCourseCellAlpha
            }

            gravity = if (styles.horizontalCourseCellText && styles.verticalCourseCellText) {
                Gravity.CENTER
            } else if (styles.verticalCourseCellText) {
                Gravity.CENTER_VERTICAL
            } else if (styles.horizontalCourseCellText) {
                Gravity.CENTER_HORIZONTAL
            } else {
                Gravity.TOP or Gravity.START
            }

            isClickable = true
            isFocusable = true

            setOnClickListener {
                courseCellClickListener?.invoke(courseCell)
            }
        }.also {
            addViewInLayout(it, -1, it.layoutParams, true)
        }
    }

    private fun initAsScheduleTimeCell(index: Int, scheduleTime: ScheduleTime) {
        val courseTimeNumText = (index + 1).toString()
        val timeTextColor = styles.getTimeTextColor(context)

        gravity = Gravity.CENTER

        TextView(context).apply {
            text = courseTimeNumText
            setTextSize(TypedValue.COMPLEX_UNIT_PX, predefine.timeCellCourseNumTextSize)
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            setTextColor(timeTextColor)
            setPadding(0, predefine.timeCellVerticalPadding, 0, predefine.timeCellVerticalPadding)

            gravity = Gravity.CENTER

            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }.also {
            addViewInLayout(it, -1, it.layoutParams, true)
        }

        if (styles.showScheduleTimes) {
            val courseTimeText = scheduleTime.startTimeStr + NEW_LINE + scheduleTime.endTimeStr
            TextView(context).apply {
                text = courseTimeText
                setTextSize(TypedValue.COMPLEX_UNIT_PX, predefine.timeCellScheduleTimeTextSize)
                setTextColor(timeTextColor)
                setPadding(0, predefine.timeCellTimeDivideTopMargin, 0, 0)

                gravity = Gravity.CENTER

                layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, predefine.timeCellTimeDivideTopMargin, 0, 0)
                }
            }.also {
                addViewInLayout(it, -1, it.layoutParams, true)
            }
        }
    }
}