package tool.xfy9326.schedule.beans

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.Px
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.getColorCompat
import tool.xfy9326.schedule.kt.spToPx

data class ScheduleStyles(
    val firstDayOfWeek: WeekDay,
    private val viewAlpha: Int,
    val forceShowWeekendColumn: Boolean,
    val showNotThisWeekCourse: Boolean,
    @ColorInt
    private val timeTextColor: Int?,
    @Px
    private val courseCellTextSize: Int,
    val cornerScreenMargin: Boolean,
    val highlightShowTodayCell: Boolean,
    @ColorInt
    private val highlightShowTodayCellColor: Int?,
    val showScheduleTimes: Boolean,
    val horizontalCourseCellText: Boolean,
    val verticalCourseCellText: Boolean,
    val notThisWeekCourseShowStyle: Set<NotThisWeekCourseShowStyle>,
) {
    val scheduleViewAlpha = viewAlpha / 100f

    fun getTimeTextColor(context: Context) = timeTextColor ?: context.getColorCompat(R.color.course_time_cell_text)

    fun getHighlightShowTodayCellColor(context: Context) = highlightShowTodayCellColor ?: context.getColorCompat(R.color.course_time_today_highlight)

    fun getCourseCellTextSize(context: Context): Float =
        courseCellTextSize.spToPx() + context.resources.getDimension(R.dimen.schedule_course_cell_text_size_offset)
}