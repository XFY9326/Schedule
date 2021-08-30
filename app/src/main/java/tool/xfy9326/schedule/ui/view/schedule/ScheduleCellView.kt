package tool.xfy9326.schedule.ui.view.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import lib.xfy9326.android.kit.setOnSingleClickListener
import lib.xfy9326.kit.NEW_LINE
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.endTimeStr
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr
import tool.xfy9326.schedule.beans.WeekDay.Companion.orderedValue
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.utils.view.ViewUtils
import kotlin.math.floor
import kotlin.properties.Delegates


@SuppressLint("ViewConstructor")
class ScheduleCellView private constructor(context: Context, private val predefine: SchedulePredefine, private val styles: ScheduleStyles) :
    LinearLayoutCompat(context) {
    private val ellipsis by lazy { context.getString(R.string.ellipsis) }
    private var courseCellClickListener: ((CourseCell) -> Unit)? = null

    var column by Delegates.notNull<Int>()
        private set

    var row by Delegates.notNull<Int>()
        private set

    var rowSpan by Delegates.notNull<Int>()
        private set

    val isTimeColumn
        get() = column == 0

    constructor(
        context: Context,
        showWeekend: Boolean,
        courseCell: CourseCell,
        schedulePredefine: SchedulePredefine,
        scheduleSettings: ScheduleStyles,
        weekStart: WeekDay,
    ) : this(context, schedulePredefine, scheduleSettings) {
        if (showWeekend || weekStart == WeekDay.MONDAY) {
            this.column = courseCell.sectionTime.weekDay.orderedValue(weekStart)
        } else {
            this.column = courseCell.sectionTime.weekDay.orderedValue(weekStart) - 1
        }
        this.row = courseCell.sectionTime.start - 1
        this.rowSpan = courseCell.sectionTime.duration

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

    init {
        alpha = styles.scheduleViewAlpha
        orientation = VERTICAL
        setPadding(predefine.gridCellPadding)
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initAsCourseCell(courseCell: CourseCell) {
        addViewPreventLayout(
            TextView(context).apply {
                text = generateCourseCellShowText(courseCell)
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                background = ViewUtils.buildBackground(courseCell.cellColor, predefine.courseCellRippleColor, predefine.courseCellBackgroundRadius)

                textSize = styles.textSize[ScheduleText.COURSE_TEXT]
                setPadding(predefine.courseCellTextPadding)

                if (styles.rowHeight != null && styles.courseCellTextLength == null) {
                    setAutoLinesForTextView(this)
                }

                setTextColor(
                    if (MaterialColorHelper.isLightColor(courseCell.cellColor)) {
                        predefine.courseCellTextColorDark
                    } else {
                        predefine.courseCellTextColorLight
                    }
                )

                if (!courseCell.isThisWeekCourse && NotThisWeekCourseShowStyle.USE_TRANSPARENT_BACKGROUND in styles.notThisWeekCourseShowStyle) {
                    alpha = styles.notThisWeekCourseAlpha
                }

                if (styles.horizontalCourseCellText && styles.verticalCourseCellText) {
                    textAlignment = View.TEXT_ALIGNMENT_INHERIT
                    gravity = Gravity.CENTER
                } else if (styles.verticalCourseCellText) {
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    gravity = Gravity.CENTER_VERTICAL
                } else if (styles.horizontalCourseCellText) {
                    textAlignment = View.TEXT_ALIGNMENT_INHERIT
                    gravity = Gravity.CENTER_HORIZONTAL
                } else {
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                }

                isClickable = true
                isFocusable = true

                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false

                setOnSingleClickListener {
                    courseCellClickListener?.invoke(courseCell)
                }
            }
        )
    }

    private fun generateCourseCellShowText(courseCell: CourseCell) =
        courseCell.courseName.appendEllipsisStyle(styles.courseCellCourseTextLength).let {
            if (courseCell.courseLocation == null || !styles.showCourseCellLocation) {
                it
            } else {
                val resId = if (styles.courseCellTextNoChangeLine) R.string.course_cell_text_no_change_line else R.string.course_cell_text
                context.getString(resId, it, courseCell.courseLocation)
            }
        }.appendEllipsisStyle(styles.courseCellTextLength).let {
            if (!courseCell.isThisWeekCourse && NotThisWeekCourseShowStyle.SHOW_NOT_THIS_WEEK_TEXT in styles.notThisWeekCourseShowStyle) {
                val notThisWeekText = context.getString(R.string.not_this_week) + NEW_LINE
                SpannableStringBuilder(notThisWeekText + it).apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, notThisWeekText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            } else {
                it
            }
        }

    private fun String.appendEllipsisStyle(textLength: Int?) =
        if (textLength != null && length > textLength) {
            substring(0, textLength) + ellipsis
        } else {
            this
        }

    private fun getTextViewDrawHeight(textView: TextView, layout: Layout) =
        layout.getLineTop(layout.lineCount) + textView.compoundPaddingTop + textView.compoundPaddingBottom

    private fun setAutoLinesForTextView(textView: TextView) {
        textView.ellipsize = TextUtils.TruncateAt.END
        viewTreeObserver.addOnPreDrawListener {
            val textLayout = textView.layout
            if (textLayout == null) {
                true
            } else {
                val drawHeight = getTextViewDrawHeight(textView, textLayout)
                if (drawHeight > measuredHeight) {
                    val lineHeight = drawHeight.toFloat() / textLayout.lineCount
                    val showLines = floor(measuredHeight / lineHeight).toInt()
                    if (showLines == textView.maxLines) {
                        true
                    } else {
                        textView.maxLines = showLines
                        false
                    }
                } else {
                    true
                }
            }
        }
    }

    private fun initAsScheduleTimeCell(index: Int, scheduleTime: ScheduleTime) {
        val courseTimeNumText = (index + 1).toString()
        val timeTextColor = styles.getTimeTextColor(context)

        gravity = Gravity.CENTER

        addViewPreventLayout(
            TextView(context).apply {
                text = courseTimeNumText
                textSize = styles.textSize[ScheduleText.SCHEDULE_NUMBER_TEXT]
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                setTextColor(timeTextColor)
                setPadding(0, predefine.timeCellVerticalPadding, 0, predefine.timeCellVerticalPadding)

                textAlignment = View.TEXT_ALIGNMENT_INHERIT
                gravity = Gravity.CENTER

                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false
            }
        )

        if (styles.showScheduleTimes) {
            val courseTimeText = scheduleTime.startTimeStr + NEW_LINE + scheduleTime.endTimeStr
            addViewPreventLayout(
                TextView(context).apply {
                    text = courseTimeText
                    textSize = styles.textSize[ScheduleText.SCHEDULE_TIME_TEXT]
                    setTextColor(timeTextColor)
                    setPadding(0, predefine.timeCellTimeDivideTopMargin, 0, 0)

                    textAlignment = View.TEXT_ALIGNMENT_INHERIT
                    gravity = Gravity.CENTER

                    layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(0, predefine.timeCellTimeDivideTopMargin, 0, 0)
                    }

                    isHorizontalScrollBarEnabled = false
                    isVerticalScrollBarEnabled = false
                }
            )
        }
    }

    private fun addViewPreventLayout(view: View) = addViewInLayout(view, -1, view.layoutParams, true)

    fun setOnCourseCellClickListener(listener: ((CourseCell) -> Unit)?) {
        this.courseCellClickListener = listener
    }
}