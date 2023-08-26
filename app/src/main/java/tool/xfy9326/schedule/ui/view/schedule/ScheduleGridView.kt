package tool.xfy9326.schedule.ui.view.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleStyles
import kotlin.math.ceil
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScheduleGridView constructor(
    context: Context,
    private val columnAmount: Int,
    private val rowAmount: Int,
    private val predefine: SchedulePredefine,
    private val styles: ScheduleStyles,
) : ViewGroup(context), IScheduleMeasure {
    private val unspecifiedHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    private var timeColumnWidth = 0
    private var courseColumnWidth = 0
    private var xRecords: IntArray = IntArray(0)
    private var rowHeight = 0

    private var courseCellClickListener: ((CourseCell) -> Unit)? = null
    private val internalCellClickListener: (CourseCell) -> Unit = {
        courseCellClickListener?.invoke(it)
    }

    override fun setMeasureConfig(timeColumnWidth: Int, courseColumnWidth: Int, xRecords: IntArray) {
        this.timeColumnWidth = timeColumnWidth
        this.courseColumnWidth = courseColumnWidth
        this.xRecords = xRecords
    }

    fun measureTimeColumnWidth(@Px maxWidth: Int): Int {
        var timeColumnWidth = 0
        val widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)

        for (child in children) {
            child as IScheduleCell

            if (child.getColumn() == 0) {
                child.measure(widthSpec, unspecifiedHeightSpec)
                timeColumnWidth = max(child.measuredWidth, timeColumnWidth)
            }
        }

        return timeColumnWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val timeColumnWidthSpec = MeasureSpec.makeMeasureSpec(timeColumnWidth, MeasureSpec.EXACTLY)
        val courseColumnWidthSpec = MeasureSpec.makeMeasureSpec(courseColumnWidth, MeasureSpec.EXACTLY)

        val styleCourseCellHeight = styles.rowHeight
        rowHeight = if (styleCourseCellHeight != null) {
            styleCourseCellHeight
        } else {
            var tempRowHeight = MeasureSpec.getSize(heightMeasureSpec) / rowAmount

            for (child in children) {
                child as IScheduleCell

                tempRowHeight = if (child.getColumn() == 0) {
                    child.measure(timeColumnWidthSpec, unspecifiedHeightSpec)
                    max(child.measuredHeight, tempRowHeight)
                } else {
                    child.measure(courseColumnWidthSpec, unspecifiedHeightSpec)
                    max(ceil(child.measuredHeight * 1f / child.getRowSpan()).toInt(), tempRowHeight)
                }
            }

            tempRowHeight
        }

        for (child in children) {
            child as IScheduleCell

            val courseColumnHeightSpec = MeasureSpec.makeMeasureSpec(rowHeight * child.getRowSpan(), MeasureSpec.EXACTLY)
            child.measure(courseColumnWidthSpec, courseColumnHeightSpec)
        }

        val bottomPadding = ViewCompat.getRootWindowInsets(this)?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0

        val actualHeight =
            if (styles.cornerScreenMargin) {
                val screenCornerMargin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val bottomLeftCorner = rootWindowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)
                    val bottomRightCorner = rootWindowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)
                    max(
                        bottomLeftCorner?.radius ?: predefine.gridBottomCornerScreenMargin,
                        bottomRightCorner?.radius ?: predefine.gridBottomCornerScreenMargin
                    )
                } else {
                    predefine.gridBottomCornerScreenMargin
                }
                rowHeight * rowAmount + max(screenCornerMargin, bottomPadding)
            } else {
                rowHeight * rowAmount + bottomPadding
            }
        val actualHeightSpec = MeasureSpec.makeMeasureSpec(actualHeight, MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec, actualHeightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) {
            val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR

            for (child in children) {
                child as IScheduleCell

                val left = xRecords[if (leftToRight) child.getColumn() else columnAmount - child.getColumn() - 1]
                val top = rowHeight * child.getRow()
                val right = xRecords[if (leftToRight) child.getColumn() + 1 else columnAmount - child.getColumn()]
                val bottom = rowHeight * (child.getRow() + child.getRowSpan())

                child.layout(left, top, right, bottom)
            }
        }
    }

    fun addScheduleCell(cellView: IScheduleCell) {
        if (cellView is View && cellView.getRow() in 0 until rowAmount && cellView.getColumn() in 0 until columnAmount) {
            addViewPreventLayout(cellView)
            if (cellView is ScheduleCourseCellView) {
                cellView.setOnCourseCellClickListener(internalCellClickListener)
            }
        }
    }

    private fun addViewPreventLayout(view: View) = addViewInLayout(view, -1, view.layoutParams, true)

    fun setOnCourseClickListener(listener: ((CourseCell) -> Unit)?) {
        this.courseCellClickListener = listener
    }

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        require(child is IScheduleCell)
        super.addView(child, index, params)
    }

    override fun addViewInLayout(child: View?, index: Int, params: LayoutParams?, preventRequestLayout: Boolean): Boolean {
        require(child is IScheduleCell)
        return super.addViewInLayout(child, index, params, preventRequestLayout)
    }
}