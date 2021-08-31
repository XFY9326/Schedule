package tool.xfy9326.schedule.ui.view.schedule

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.core.view.setPadding
import lib.xfy9326.android.kit.getStringArray
import lib.xfy9326.android.kit.spToPx
import lib.xfy9326.kit.NEW_LINE
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.utils.view.ViewUtils
import kotlin.math.max

class ScheduleHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    private var styles: ScheduleStyles? = null
    private var columnAmount = 0
    private var predefine: SchedulePredefine? = null
    private var days: Array<Day>? = null

    private val unspecifiedSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    private val weekDayStrArr = context.getStringArray(R.array.weekday)

    private var headerHeight = minimumHeight
    private var timeColumnWidth: Int = 0
    private var courseColumnWidth: Int = 0
    private lateinit var xRecords: IntArray

    init {
        alpha = styles?.scheduleViewAlpha ?: 1f
    }

    fun setDays(days: Array<Day>) {
        val styles = this.styles
        val predefine = this.predefine
        if (styles != null && predefine != null && !days.contentEquals(this.days)) {
            buildMonthView(days[0].month, styles).also {
                addViewInLayout(it, -1, it.layoutParams, true)
            }
            repeat(days.size) { i ->
                buildDayView(days[i], styles, predefine).also {
                    addViewInLayout(it, -1, it.layoutParams, true)
                }
            }
            columnAmount = days.size + 1
            this.days = days
            requestLayout()
        }
    }

    fun setScheduleViewData(viewData: ScheduleViewData) {
        if (styles != viewData.styles) {
            styles = viewData.styles
            requestLayout()
        }
    }

    fun setSchedulePredefine(schedulePredefine: SchedulePredefine) {
        if (predefine != schedulePredefine) {
            predefine = schedulePredefine
            requestLayout()
        }
    }

    fun setMeasureConfig(timeColumnWidth: Int, courseColumnWidth: Int, xRecords: IntArray) {
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
        val doublePadding = (predefine?.gridCellPadding ?: 0) * 2
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(timeColumnWidth - doublePadding, MeasureSpec.EXACTLY)
        val dayWidthSpec = MeasureSpec.makeMeasureSpec(courseColumnWidth - doublePadding, MeasureSpec.EXACTLY)

        headerHeight = 0

        for ((i, view) in children.withIndex()) {
            view.measure(if (i == 0) monthWidthSpec else dayWidthSpec, unspecifiedSpec)
            headerHeight = max(headerHeight, view.measuredHeight)
        }

        for ((i, view) in children.withIndex()) {
            view.measure(if (i == 0) monthWidthSpec else dayWidthSpec, MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY))
        }

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(headerHeight + doublePadding, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val gridCellPadding = predefine?.gridCellPadding ?: 0
        if (childCount > 0) {
            val height = b - t
            val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR

            for (i in 0 until columnAmount) {
                val view = getChildAt(i)
                if (leftToRight) {
                    view.layout(
                        xRecords[i] + gridCellPadding,
                        gridCellPadding,
                        xRecords[i + 1] - gridCellPadding,
                        height - gridCellPadding
                    )
                } else {
                    view.layout(
                        xRecords[columnAmount - i - 1] + gridCellPadding,
                        gridCellPadding,
                        xRecords[columnAmount - i] - gridCellPadding,
                        height - gridCellPadding
                    )
                }
            }
        }
    }
}