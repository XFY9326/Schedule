package tool.xfy9326.schedule.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.LinearLayoutCompat
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.ScheduleViewData
import kotlin.math.floor
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScheduleView(
    context: Context,
    scheduleViewData: ScheduleViewData,
    scheduleStyles: ScheduleStyles,
) : LinearLayoutCompat(context) {
    companion object {
        private const val MIN_SCHEDULE_COLUMN_COUNT = 6
        private const val MAX_SCHEDULE_COLUMN_COUNT = 8

        fun buildBackground(@ColorInt contentColorInt: Int, @ColorInt rippleColorInt: Int, @Px radius: Float): Drawable {
            val content = GradientDrawable().apply {
                if (radius != 0f) cornerRadius = radius
                setColor(contentColorInt)
            }
            return RippleDrawable(ColorStateList.valueOf(rippleColorInt), content, null)
        }
    }

    private val schedulePredefine = SchedulePredefine.load(context)

    private val showWeekend = scheduleStyles.forceShowWeekendColumn || scheduleViewData.hasWeekendCourse
    private val columnAmount =
        if (showWeekend) {
            MAX_SCHEDULE_COLUMN_COUNT
        } else {
            MIN_SCHEDULE_COLUMN_COUNT
        }

    private val scheduleHeaderView = ScheduleHeaderView(
        context,
        scheduleViewData.weekNum,
        scheduleViewData.startDate,
        showWeekend,
        scheduleStyles,
        schedulePredefine
    ).also {
        addViewInLayout(it, -1, it.layoutParams, true)
    }
    private val scheduleGridView = ScheduleGridView(
        context,
        showWeekend,
        columnAmount,
        scheduleViewData,
        scheduleStyles,
        schedulePredefine
    )

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        ScheduleScrollView(context, scheduleGridView).also {
            addViewInLayout(it, -1, it.layoutParams, true)
        }

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val targetWidth = MeasureSpec.getSize(widthMeasureSpec)

        val timeColumnWidth = max(scheduleHeaderView.measureMonthViewWidth(targetWidth), scheduleGridView.measureTimeColumnWidth(targetWidth))
        val courseColumnWidth = floor((targetWidth - timeColumnWidth) * 1f / (columnAmount - 1)).toInt()

        scheduleHeaderView.setMeasureConfig(timeColumnWidth, courseColumnWidth)
        scheduleGridView.setMeasureConfig(timeColumnWidth, courseColumnWidth)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("ViewConstructor")
    private class ScheduleScrollView(context: Context, scheduleGridView: ScheduleGridView) : ScrollView(context) {
        init {
            isFillViewport = true
            overScrollMode = OVER_SCROLL_NEVER
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addViewInLayout(scheduleGridView, -1, scheduleGridView.layoutParams, true)
        }
    }

    fun setOnCourseClickListener(listener: ((CourseCell) -> Unit)?) = scheduleGridView.setOnCourseClickListener(listener)
}