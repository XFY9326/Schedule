package tool.xfy9326.schedule.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.ScheduleViewData
import kotlin.math.ceil
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScheduleGridView(
    context: Context,
    showWeekend: Boolean,
    private val columnAmount: Int,
    viewData: ScheduleViewData,
    private val styles: ScheduleStyles,
    private val predefine: SchedulePredefine,
) : ViewGroup(context) {
    companion object {
        private const val LOG_TAG = "ScheduleGridView"
        private val unspecifiedHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    }

    private val rowAmount = viewData.times.size

    private var timeColumnWidth = 0
    private var courseColumnWidth = 0
    private var rowHeight = 0

    private var courseCellClickListener: ((CourseCell) -> Unit)? = null
    private val internalCellClickListener: (CourseCell) -> Unit = {
        courseCellClickListener?.invoke(it)
    }

    init {
        for ((i, time) in viewData.times.withIndex()) {
            ScheduleCellView(context, i, time, predefine, styles).let {
                addViewInLayout(it, -1, it.layoutParams, true)
            }
        }

        for (cell in viewData.cells) {
            ScheduleCellView(context, showWeekend, cell, predefine, styles, viewData.weekStart, internalCellClickListener).let {
                if (it.row in 0 until rowAmount) {
                    if (it.column in 0 until columnAmount) {
                        addViewInLayout(it, -1, it.layoutParams, true)
                    } else {
                        Log.w(LOG_TAG, "Course column error! It should in 0..${columnAmount - 1} but now it's ${it.column}. Skip it!")
                    }
                } else {
                    Log.w(LOG_TAG, "Course row error! It should in 0..${rowAmount - 1} but now it's ${it.row}. Skip it!")
                }
            }
        }

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setMeasureConfig(timeColumnWidth: Int, courseColumnWidth: Int) {
        this.timeColumnWidth = timeColumnWidth
        this.courseColumnWidth = courseColumnWidth
    }

    fun measureTimeColumnWidth(@Px maxWidth: Int): Int {
        var timeColumnWidth = 0
        val widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)

        for (child in children) {
            child as ScheduleCellView

            if (child.column == 0) {
                child.measure(widthSpec, unspecifiedHeightSpec)
                timeColumnWidth = max(child.measuredWidth, timeColumnWidth)
            }
        }

        return timeColumnWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val timeColumnWidthSpec = MeasureSpec.makeMeasureSpec(timeColumnWidth, MeasureSpec.EXACTLY)
        val courseColumnWidthSpec = MeasureSpec.makeMeasureSpec(courseColumnWidth, MeasureSpec.EXACTLY)

        rowHeight = MeasureSpec.getSize(heightMeasureSpec) / rowAmount

        for (child in children) {
            child as ScheduleCellView

            rowHeight = if (child.column == 0) {
                child.measure(timeColumnWidthSpec, unspecifiedHeightSpec)
                max(child.measuredHeight, rowHeight)
            } else {
                child.measure(courseColumnWidthSpec, unspecifiedHeightSpec)
                max(ceil(child.measuredHeight * 1f / child.rowSpan).toInt(), rowHeight)
            }
        }

        for (child in children) {
            child as ScheduleCellView

            val courseColumnHeightSpec = MeasureSpec.makeMeasureSpec(rowHeight * child.rowSpan, MeasureSpec.EXACTLY)
            child.measure(courseColumnWidthSpec, courseColumnHeightSpec)
        }

        val bottomInset =
            rootWindowInsets?.let {
                WindowInsetsCompat.toWindowInsetsCompat(it).systemWindowInsetBottom
            } ?: 0

        val actualHeight =
            if (styles.cornerScreenMargin) {
                rowHeight * rowAmount + predefine.gridBottomCornerScreenMargin + bottomInset
            } else {
                rowHeight * rowAmount + bottomInset
            }
        val actualHeightSpec = MeasureSpec.makeMeasureSpec(actualHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, actualHeightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) layoutCells(l, t)
    }

    private fun layoutCells(l: Int, t: Int) {
        val yRecord = Array(rowAmount + 1) {
            t + rowHeight * it
        }
        var xTemp = l
        val leftToRight = context.resources.configuration.layoutDirection == LAYOUT_DIRECTION_LTR
        val xRecord = Array(columnAmount + 1) {
            if (it == 0) return@Array 0
            xTemp += if (leftToRight) {
                when (it) {
                    1 -> timeColumnWidth
                    else -> courseColumnWidth
                }
            } else {
                when (it) {
                    columnAmount -> timeColumnWidth
                    else -> courseColumnWidth
                }
            }
            xTemp
        }

        for (child in children) {
            child as ScheduleCellView

            if (leftToRight) {
                child.layout(
                    xRecord[child.column],
                    yRecord[child.row],
                    xRecord[child.column + 1],
                    yRecord[child.row + child.rowSpan]
                )
            } else {
                child.layout(
                    xRecord[columnAmount - child.column - 1],
                    yRecord[child.row],
                    xRecord[columnAmount - child.column],
                    yRecord[child.row + child.rowSpan]
                )
            }
        }
    }

    fun setOnCourseClickListener(listener: ((CourseCell) -> Unit)?) {
        this.courseCellClickListener = listener
    }
}