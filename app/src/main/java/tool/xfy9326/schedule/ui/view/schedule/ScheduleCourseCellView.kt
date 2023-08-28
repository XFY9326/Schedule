package tool.xfy9326.schedule.ui.view.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.setPadding
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.beans.WeekDay.Companion.orderedValue
import tool.xfy9326.schedule.kt.asStringBuilder
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.utils.NEW_LINE
import tool.xfy9326.schedule.utils.view.ViewUtils
import kotlin.math.floor

@SuppressLint("ViewConstructor")
class ScheduleCourseCellView(
    context: Context,
    private val showWeekend: Boolean,
    private val courseCell: CourseCell,
    private val predefine: SchedulePredefine,
    private val styles: ScheduleStyles,
    private val weekStart: WeekDay,
) : FrameLayout(context), IScheduleCell {

    init {
        alpha = styles.scheduleViewAlpha
        setPadding(
            styles.courseCellHorizontalPadding,
            styles.courseCellVerticalPadding,
            styles.courseCellHorizontalPadding,
            styles.courseCellVerticalPadding
        )
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        initView().let {
            it.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addViewInLayout(it, -1, it.layoutParams, true)
        }
    }

    private val innerColumn by lazy {
        if (showWeekend || weekStart == WeekDay.MONDAY) {
            courseCell.sectionTime.weekDay.orderedValue(weekStart)
        } else {
            courseCell.sectionTime.weekDay.orderedValue(weekStart) - 1
        }
    }
    private val ellipsisText by lazy {
        context.getString(R.string.ellipsis)
    }
    private var cellClickListener: ((CourseCell) -> Unit)? = null

    override fun getColumn(): Int = innerColumn

    override fun getRow(): Int = courseCell.sectionTime.start - 1

    override fun getRowSpan(): Int = courseCell.sectionTime.duration

    fun setOnCourseCellClickListener(listener: ((CourseCell) -> Unit)?) {
        this.cellClickListener = listener
    }

    private fun initView() =
        TextView(context).apply {
            text = generateCourseCellShowText(courseCell)
            background = ViewUtils.buildBackground(courseCell.cellColor, predefine.courseCellRippleColor, predefine.courseCellBackgroundRadius)

            textSize = styles.textSize[ScheduleText.COURSE_TEXT]
            setPadding(predefine.courseCellTextPadding)

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

            if (styles.rowHeight != null && styles.courseCellTextLength == null) {
                ellipsize = TextUtils.TruncateAt.END
                val targetHeight = getRowSpan() * styles.rowHeight - styles.courseCellVerticalPadding * 2 - compoundPaddingTop - compoundPaddingBottom
                val lineHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
                maxLines = floor(targetHeight / lineHeight).toInt()
            }

            isClickable = true
            isFocusable = true

            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false

            setOnSingleClickListener {
                cellClickListener?.invoke(courseCell)
            }
        }

    private fun generateCourseCellShowText(courseCell: CourseCell): CharSequence =
        courseCell.courseName.asStringBuilder().appendEllipsisStyle(styles.courseCellCourseTextLength).apply {
            if (styles.courseCellDetailContent.isNotEmpty()) {
                if (CourseCellDetailContent.LOCATION in styles.courseCellDetailContent && courseCell.courseLocation != null) {
                    appendLine()
                    if (!styles.courseCellTextNoNewLine) appendLine()
                    append(context.getString(R.string.course_cell_location, courseCell.courseLocation))
                }
                if (CourseCellDetailContent.TEACHER in styles.courseCellDetailContent && courseCell.courseTeacher != null) {
                    appendLine()
                    append(courseCell.courseTeacher)
                }
            }
        }.appendEllipsisStyle(styles.courseCellTextLength).toString().let {
            if (!courseCell.isThisWeekCourse && NotThisWeekCourseShowStyle.SHOW_NOT_THIS_WEEK_TEXT in styles.notThisWeekCourseShowStyle) {
                val notThisWeekText = context.getString(R.string.not_this_week) + NEW_LINE
                SpannableStringBuilder().apply {
                    append(notThisWeekText, StyleSpan(Typeface.BOLD), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    append(it)
                }
            } else {
                it
            }
        }

    private fun StringBuilder.appendEllipsisStyle(textLength: Int?): StringBuilder =
        if (textLength != null && length > textLength) {
            setLength(textLength)
            append(ellipsisText)
            this
        } else {
            this
        }
}