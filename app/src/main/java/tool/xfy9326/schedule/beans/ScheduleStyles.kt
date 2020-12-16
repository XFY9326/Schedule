package tool.xfy9326.schedule.beans

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.getColorCompat

data class ScheduleStyles(
    val firstDayOfWeek: WeekDay,
    @FloatRange(from = 0.0, to = 1.0)
    val viewAlpha: Float,
    val forceShowWeekendColumn: Boolean,
    val showNotThisWeekCourse: Boolean,
    @ColorInt
    private val timeTextColor: Int?,
    @Px
    private val courseCellTextSize: Float?,
    val cornerScreenMargin: Boolean,
) {
    fun getTimeTextColor(context: Context) = timeTextColor ?: context.getColorCompat(R.color.course_time_cell_text_color)

    fun getCourseCellTextSize(context: Context) = courseCellTextSize ?: context.resources.getDimension(R.dimen.schedule_course_cell_text_size)
}