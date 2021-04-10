package tool.xfy9326.schedule.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Px
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleViewData
import tool.xfy9326.schedule.kt.getStringArray
import tool.xfy9326.schedule.utils.view.ViewUtils
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScheduleHeaderView(
    context: Context,
    scheduleViewData: ScheduleViewData,
    private val days: Array<Day>,
    private val predefine: SchedulePredefine,
) : ViewGroup(context) {
    private val unspecifiedHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    private val styles = scheduleViewData.styles
    private val weekDayStrArr = context.getStringArray(R.array.weekday)
    private val monthView = buildMonthView(days[0].month).also {
        addViewInLayout(it, -1, it.layoutParams, true)
    }
    private val dayViews = Array(days.size) { i ->
        buildDayView(days[i]).also {
            addViewInLayout(it, -1, it.layoutParams, true)
        }
    }
    private val columnAmount = days.size + 1

    private var headerHeight = minimumHeight
    private var timeColumnWidth: Int = 0
    private var courseColumnWidth: Int = 0
    private lateinit var xRecords: IntArray

    init {
        alpha = styles.scheduleViewAlpha
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setMeasureConfig(timeColumnWidth: Int, courseColumnWidth: Int, xRecords: IntArray) {
        this.timeColumnWidth = timeColumnWidth
        this.courseColumnWidth = courseColumnWidth
        this.xRecords = xRecords
    }

    private fun buildMonthView(month: Int) =
        TextView(context).apply {
            text = context.getString(R.string.month, month)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, predefine.monthTextSize)
            gravity = Gravity.CENTER
            setTextColor(styles.getTimeTextColor(context))
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)

            textAlignment = View.TEXT_ALIGNMENT_INHERIT
            gravity = Gravity.CENTER

            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }

    private fun buildDayView(day: Day) =
        LinearLayoutCompat(context).apply {
            val isToday = day.isToday()
            val timeTextColor = styles.getTimeTextColor(context)

            orientation = LinearLayoutCompat.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(predefine.courseCellTextPadding / 2)

            if (styles.highlightShowTodayCell && isToday) {
                background = ViewUtils.buildBackground(
                    styles.getHighlightShowTodayCellColor(context),
                    predefine.courseCellRippleColor,
                    predefine.courseCellBackgroundRadius
                )
            }

            addView(TextView(context).apply {
                text = context.getString(R.string.month_date_simple, day.month, day.day)
                setTextColor(timeTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, predefine.monthDateTextSize)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            })
            addView(TextView(context).apply {
                text = weekDayStrArr[day.weekDay.ordinal]
                setTextColor(timeTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, predefine.weekDayTextSize)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                layoutParams = LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, predefine.timeCellTimeDivideTopMargin, 0, 0)
                }
            })
        }

    fun measureMonthViewWidth(@Px maxWidth: Int): Int {
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)
        monthView.measure(monthWidthSpec, unspecifiedHeightSpec)
        return monthView.measuredWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val doublePadding = predefine.gridCellPadding * 2
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(timeColumnWidth - doublePadding, MeasureSpec.EXACTLY)
        val dayWidthSpec = MeasureSpec.makeMeasureSpec(courseColumnWidth - doublePadding, MeasureSpec.EXACTLY)

        headerHeight = 0

        monthView.measure(monthWidthSpec, unspecifiedHeightSpec)
        headerHeight = max(headerHeight, monthView.measuredHeight)
        for (dayView in dayViews) {
            dayView.measure(dayWidthSpec, unspecifiedHeightSpec)
            headerHeight = max(headerHeight, dayView.measuredHeight)
        }

        monthView.measure(monthWidthSpec, MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY))
        for (dayView in dayViews) {
            dayView.measure(dayWidthSpec, MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY))
        }

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(headerHeight + doublePadding, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) {
            val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR

            for (i in 0 until columnAmount) {
                val view = if (i == 0) {
                    monthView
                } else {
                    dayViews[i - 1]
                }
                if (leftToRight) {
                    view.layout(
                        xRecords[i] + predefine.gridCellPadding,
                        predefine.gridCellPadding,
                        xRecords[i + 1] - predefine.gridCellPadding,
                        measuredHeight - predefine.gridCellPadding
                    )
                } else {
                    view.layout(
                        xRecords[columnAmount - i - 1] + predefine.gridCellPadding,
                        predefine.gridCellPadding,
                        xRecords[columnAmount - i] - predefine.gridCellPadding,
                        measuredHeight - predefine.gridCellPadding
                    )
                }
            }
        }
    }
}