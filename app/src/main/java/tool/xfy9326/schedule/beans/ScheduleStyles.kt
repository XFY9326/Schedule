package tool.xfy9326.schedule.beans

import android.content.Context
import androidx.annotation.ColorInt
import lib.xfy9326.android.kit.getColorCompat
import tool.xfy9326.schedule.R

data class ScheduleStyles(
    private val viewAlpha: Int,
    val forceShowWeekendColumn: Boolean,
    val showNotThisWeekCourse: Boolean,
    @ColorInt
    private val timeTextColor: Int?,
    val cornerScreenMargin: Boolean,
    val highlightShowTodayCell: Boolean,
    @ColorInt
    private val highlightShowTodayCellColor: Int?,
    val showScheduleTimes: Boolean,
    val horizontalCourseCellText: Boolean,
    val verticalCourseCellText: Boolean,
    val notThisWeekCourseShowStyle: Set<NotThisWeekCourseShowStyle>,
    val enableScheduleGridScroll: Boolean,
    val textSize: ScheduleText.TextSize,
    private val notThisWeekCourseCellAlpha: Int,
) {
    val scheduleViewAlpha = viewAlpha / 100f

    val notThisWeekCourseAlpha = notThisWeekCourseCellAlpha / 100f

    fun getTimeTextColor(context: Context) = timeTextColor ?: context.getColorCompat(R.color.course_time_cell_text)

    fun getHighlightShowTodayCellColor(context: Context) = highlightShowTodayCellColor ?: context.getColorCompat(R.color.course_time_today_highlight)
}