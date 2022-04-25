package tool.xfy9326.schedule.ui.view.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.core.view.setPadding
import io.github.xfy9326.atools.ui.getStringArray
import io.github.xfy9326.atools.ui.spToPx
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.ScheduleText
import tool.xfy9326.schedule.utils.NEW_LINE
import tool.xfy9326.schedule.utils.view.ViewUtils
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScheduleHeaderView constructor(
    context: Context,
    private val days: Array<Day>,
    private val predefine: SchedulePredefine,
    private val styles: ScheduleStyles,
) : ViewGroup(context), IScheduleMeasure {
    private val columnAmount: Int

    private val unspecifiedSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    private val weekDayStrArr = context.getStringArray(R.array.weekday)

    private var headerHeight = minimumHeight
    private var timeColumnWidth: Int = 0
    private var courseColumnWidth: Int = 0
    private var xRecords: IntArray = IntArray(0)

    init {
        alpha = styles.scheduleViewAlpha
        buildMonthView(days[0].month, styles).also {
            addViewInLayout(it, -1, it.layoutParams, true)
        }
        repeat(days.size) { i ->
            buildDayView(days[i], styles, predefine).also {
                addViewInLayout(it, -1, it.layoutParams, true)
            }
        }
        columnAmount = days.size + 1
    }

    override fun setMeasureConfig(timeColumnWidth: Int, courseColumnWidth: Int, xRecords: IntArray) {
        this.timeColumnWidth = timeColumnWidth
        this.courseColumnWidth = courseColumnWidth
        this.xRecords = xRecords
    }

    private fun buildMonthView(month: Int, styles: ScheduleStyles) =
        AppCompatTextView(context).apply {
            text = context.getString(R.string.month, month)
            textSize = styles.textSize[ScheduleText.HEADER_MONTH_TEXT]
            setTextColor(styles.getTimeTextColor(context))
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)

            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_GRAVITY

            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

    private fun buildDayView(day: Day, styles: ScheduleStyles, predefine: SchedulePredefine) =
        AppCompatTextView(context).apply {
            val isToday = day.isToday
            setTextColor(styles.getTimeTextColor(context))
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)

            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_GRAVITY

            setPadding(predefine.courseCellTextPadding / 2)

            if (styles.highlightShowTodayCell && isToday) {
                background = ViewUtils.buildBackground(
                    styles.getHighlightShowTodayCellColor(context),
                    predefine.courseCellRippleColor,
                    predefine.courseCellBackgroundRadius
                )
            }

            val monthText = context.getString(R.string.month_date_simple, day.month, day.day)
            val weekDayText = weekDayStrArr[day.weekDay.ordinal]

            text = SpannableStringBuilder().apply {
                append(monthText, AbsoluteSizeSpan(styles.textSize[ScheduleText.HEADER_MONTH_DATE_TEXT].spToPx().toInt()), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                append(NEW_LINE)
                append(weekDayText, AbsoluteSizeSpan(styles.textSize[ScheduleText.HEADER_WEEKDAY_TEXT].spToPx().toInt()), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            setLineSpacing(predefine.timeCellTimeDivideTopMargin.toFloat(), 1f)

            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

    fun measureMonthViewWidth(@Px maxWidth: Int): Int {
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)
        return getChildAt(0).run {
            measure(monthWidthSpec, unspecifiedSpec)
            measuredWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val verticalDoublePadding = styles.courseCellVerticalPadding * 2
        val horizontalDoublePadding = styles.courseCellHorizontalPadding * 2
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(timeColumnWidth - horizontalDoublePadding, MeasureSpec.EXACTLY)
        val dayWidthSpec = MeasureSpec.makeMeasureSpec(courseColumnWidth - horizontalDoublePadding, MeasureSpec.EXACTLY)

        headerHeight = 0

        for ((i, view) in children.withIndex()) {
            view.measure(if (i == 0) monthWidthSpec else dayWidthSpec, unspecifiedSpec)
            headerHeight = max(headerHeight, view.measuredHeight)
        }

        for ((i, view) in children.withIndex()) {
            view.measure(if (i == 0) monthWidthSpec else dayWidthSpec, MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY))
        }

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(headerHeight + verticalDoublePadding, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) {
            val height = b - t
            val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR

            for (i in 0 until columnAmount) {
                val view = getChildAt(i)
                val left = xRecords[if (leftToRight) i else columnAmount - i - 1] + styles.courseCellHorizontalPadding
                val right = xRecords[if (leftToRight) i + 1 else columnAmount - i] - styles.courseCellHorizontalPadding
                view.layout(left, styles.courseCellVerticalPadding, right, height - styles.courseCellVerticalPadding)
            }
        }
    }
}