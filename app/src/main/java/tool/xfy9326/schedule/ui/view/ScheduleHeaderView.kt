package tool.xfy9326.schedule.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Px
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.kt.getStringArray
import tool.xfy9326.schedule.utils.CourseTimeUtils
import java.util.*
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScheduleHeaderView(
    context: Context,
    weekNum: Int,
    startDate: Date,
    showWeekend: Boolean,
    private val styles: ScheduleStyles,
    private val predefine: SchedulePredefine,
) : ViewGroup(context) {
    companion object {
        private val unspecifiedHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    }

    private val days = CourseTimeUtils.getDayInWeek(weekNum, startDate, styles.firstDayOfWeek, showWeekend)
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

    init {
        alpha = styles.viewAlpha
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setMeasureConfig(timeColumnWidth: Int, courseColumnWidth: Int) {
        this.timeColumnWidth = timeColumnWidth
        this.courseColumnWidth = courseColumnWidth
    }

    private fun buildMonthView(month: Int) =
        LinearLayoutCompat(context).apply {
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            setPadding(predefine.gridCellPadding)

            addView(TextView(context).apply {
                text = context.getString(R.string.month, month)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, predefine.monthTextSize)
                gravity = Gravity.CENTER
                setTextColor(styles.getTimeTextColor(context))
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)

                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            })
        }


    private fun buildDayView(day: Day) =
        LinearLayoutCompat(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            setPadding(predefine.gridCellPadding)

            val isToday = day.isToday()
            val timeTextColor = styles.getTimeTextColor(context)

            addView(LinearLayoutCompat(context).apply {
                orientation = LinearLayoutCompat.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                setPadding(predefine.courseCellTextPadding / 2)

                if (isToday) {
                    background = ScheduleView.buildBackground(
                        predefine.courseTimeCellTodayColor,
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
            })
        }

    fun measureMonthViewWidth(@Px maxWidth: Int): Int {
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)
        monthView.measure(monthWidthSpec, unspecifiedHeightSpec)
        return monthView.measuredWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(timeColumnWidth, MeasureSpec.EXACTLY)
        val dayWidthSpec = MeasureSpec.makeMeasureSpec(courseColumnWidth, MeasureSpec.EXACTLY)

        headerHeight = 0

        monthView.measure(monthWidthSpec, unspecifiedHeightSpec)
        headerHeight = max(headerHeight, monthView.measuredHeight)
        for (dayView in dayViews) {
            dayView.measure(dayWidthSpec, unspecifiedHeightSpec)
            headerHeight = max(headerHeight, dayView.measuredHeight)
        }

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) layoutChildren(l, t)
    }

    private fun layoutChildren(l: Int, t: Int) {
        var xTemp = l
        val leftToRight = context.resources.configuration.layoutDirection == LAYOUT_DIRECTION_LTR
        val xRecord = Array(columnAmount + 1) {
            if (it == 0) return@Array 0
            xTemp += if (leftToRight) {
                when (it) {
                    1 -> monthView.measuredWidth
                    else -> dayViews[it - 2].measuredWidth
                }
            } else {
                when (it) {
                    columnAmount -> monthView.measuredWidth
                    else -> dayViews[it - 1].measuredWidth
                }
            }
            xTemp
        }

        val monthX = getChildX(xRecord, leftToRight, 0)
        monthView.layout(monthX.first, t, monthX.second, measuredHeight)

        for ((i, dayView) in dayViews.withIndex()) {
            val dayX = getChildX(xRecord, leftToRight, i + 1)
            dayView.layout(dayX.first, t, dayX.second, measuredHeight)
        }
    }

    private fun getChildX(xRecord: Array<Int>, leftToRight: Boolean, column: Int) =
        if (leftToRight) {
            xRecord[column] to xRecord[column + 1]
        } else {
            xRecord[columnAmount - column - 1] to xRecord[columnAmount - column]
        }
}