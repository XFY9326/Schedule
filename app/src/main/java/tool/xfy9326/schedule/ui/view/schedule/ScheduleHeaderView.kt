package tool.xfy9326.schedule.ui.view.schedule

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Px
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.core.view.setPadding
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.ScheduleViewData
import tool.xfy9326.schedule.kt.getStringArray
import tool.xfy9326.schedule.utils.view.ViewUtils
import kotlin.math.max

class ScheduleHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    private var styles: ScheduleStyles? = null
    private var columnAmount = 0
    private var predefine: SchedulePredefine? = null
    private var days: Array<Day>? = null

    private val unspecifiedHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
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
        TextView(context).apply {
            text = context.getString(R.string.month, month)
            textSize = styles.textSize.getHeaderMonthTextSize(context)
            gravity = Gravity.CENTER
            setTextColor(styles.getTimeTextColor(context))
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)

            textAlignment = View.TEXT_ALIGNMENT_INHERIT
            gravity = Gravity.CENTER

            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }

    private fun buildDayView(day: Day, styles: ScheduleStyles, predefine: SchedulePredefine) =
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
                textSize = styles.textSize.getHeaderMonthDateTextSize(context)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            })
            addView(TextView(context).apply {
                text = weekDayStrArr[day.weekDay.ordinal]
                setTextColor(timeTextColor)
                textSize = styles.textSize.getHeaderWeekdayTextSize(context)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                layoutParams = LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, predefine.timeCellTimeDivideTopMargin, 0, 0)
                }
            })
        }

    fun measureMonthViewWidth(@Px maxWidth: Int): Int {
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)
        return getChildAt(0).run {
            measure(monthWidthSpec, unspecifiedHeightSpec)
            measuredWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val doublePadding = (predefine?.gridCellPadding ?: 0) * 2
        val monthWidthSpec = MeasureSpec.makeMeasureSpec(timeColumnWidth - doublePadding, MeasureSpec.EXACTLY)
        val dayWidthSpec = MeasureSpec.makeMeasureSpec(courseColumnWidth - doublePadding, MeasureSpec.EXACTLY)

        headerHeight = 0

        for ((i, view) in children.withIndex()) {
            headerHeight = if (i == 0) {
                view.measure(monthWidthSpec, unspecifiedHeightSpec)
                max(headerHeight, view.measuredHeight)
            } else {
                view.measure(dayWidthSpec, unspecifiedHeightSpec)
                max(headerHeight, view.measuredHeight)
            }
        }

        for ((i, view) in children.withIndex()) {
            if (i == 0) {
                view.measure(monthWidthSpec, MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY))
            } else {
                view.measure(dayWidthSpec, MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY))
            }
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