package tool.xfy9326.schedule.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.ScheduleStyles
import kotlin.math.floor
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScheduleView(
    context: Context,
    scheduleStyles: ScheduleStyles,
    private val columnAmount: Int,
    private val scheduleHeaderView: ScheduleHeaderView,
    private val scheduleGridView: ScheduleGridView,
) : LinearLayoutCompat(context) {

    init {
        orientation = VERTICAL

        addViewPreventLayout(scheduleHeaderView)
        if (scheduleStyles.enableScheduleGridScroll) {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addViewPreventLayout(ScheduleScrollLayout(context, scheduleGridView))
        } else {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            addViewPreventLayout(scheduleGridView)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val targetWidth = MeasureSpec.getSize(widthMeasureSpec)

        val timeColumnWidth = max(scheduleHeaderView.measureMonthViewWidth(targetWidth), scheduleGridView.measureTimeColumnWidth(targetWidth))
        val courseColumnWidth = floor((targetWidth - timeColumnWidth) * 1f / (columnAmount - 1)).toInt()
        val xRecords = getXRecords(timeColumnWidth, courseColumnWidth)

        scheduleHeaderView.setMeasureConfig(timeColumnWidth, courseColumnWidth, xRecords)
        scheduleGridView.setMeasureConfig(timeColumnWidth, courseColumnWidth, xRecords)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getXRecords(timeColumnWidth: Int, courseColumnWidth: Int): IntArray {
        var xTemp = 0
        val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR
        return IntArray(columnAmount + 1) {
            if (it != 0) xTemp += if (leftToRight && it == 1 || !leftToRight && it == columnAmount) timeColumnWidth else courseColumnWidth
            xTemp
        }
    }

    private fun addViewPreventLayout(view: View) = addViewInLayout(view, -1, view.layoutParams, true)

    fun setOnCourseClickListener(listener: ((CourseCell) -> Unit)?) = scheduleGridView.setOnCourseClickListener(listener)
}