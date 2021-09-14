package tool.xfy9326.schedule.ui.view.schedule

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.RoundedCorner
import android.view.View
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

class ScheduleGridView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private var styles: ScheduleStyles? = null
    private var rowAmount = 0
    private var columnAmount = 0
    private var predefine: SchedulePredefine? = null

    private val unspecifiedHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    private var timeColumnWidth = 0
    private var courseColumnWidth = 0
    private lateinit var xRecords: IntArray
    private var rowHeight = 0

    private var courseCellClickListener: ((CourseCell) -> Unit)? = null
    private val internalCellClickListener: (CourseCell) -> Unit = {
        courseCellClickListener?.invoke(it)
    }

    fun setSchedulePredefine(schedulePredefine: SchedulePredefine) {
        if (predefine != schedulePredefine) {
            predefine = schedulePredefine
            requestLayout()
        }
    }

    fun setScheduleViewData(viewData: ScheduleViewData) {
        if (styles != viewData.styles || rowAmount != viewData.times.size) {
            styles = viewData.styles
            rowAmount = viewData.times.size
            requestLayout()
        }
    }

    fun setColumnAmount(num: Int) {
        if (columnAmount != num) {
            columnAmount = num
            requestLayout()
        }
    }

    fun setMeasureConfig(timeColumnWidth: Int, courseColumnWidth: Int, xRecords: IntArray) {
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

        val styleCourseCellHeight = styles?.rowHeight
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

        val bottomInset =
            rootWindowInsets?.let {
                WindowInsetsCompat.toWindowInsetsCompat(it).getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            } ?: 0

        val actualHeight =
            if (styles?.cornerScreenMargin == true) {
                val defaultScreenCornerMargin = predefine?.gridBottomCornerScreenMargin ?: 0
                val screenCornerMargin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val bottomLeftCorner = rootWindowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)
                    val bottomRightCorner = rootWindowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)
                    max(bottomLeftCorner?.radius ?: defaultScreenCornerMargin, bottomRightCorner?.radius ?: defaultScreenCornerMargin)
                } else {
                    defaultScreenCornerMargin
                }
                rowHeight * rowAmount + max(screenCornerMargin, bottomInset)
            } else {
                rowHeight * rowAmount + bottomInset
            }
        val actualHeightSpec = MeasureSpec.makeMeasureSpec(actualHeight, MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec, actualHeightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount > 0) {
            val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR

            for (child in children) {
                child as IScheduleCell

                val startY = rowHeight * child.getRow()
                val endY = rowHeight * (child.getRow() + child.getRowSpan())

                if (leftToRight) {
                    child.layout(xRecords[child.getColumn()], startY, xRecords[child.getColumn() + 1], endY)
                } else {
                    child.layout(xRecords[columnAmount - child.getColumn() - 1], startY, xRecords[columnAmount - child.getColumn()], endY)
                }
            }
        }
    }

    fun addScheduleCellPreventLayout(cellView: IScheduleCell) {
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