package tool.xfy9326.schedule.beans

import android.content.Context
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.Sp

data class ScheduleTextSize(
    @Sp
    private val courseTextSize: Int,
    @Sp
    private val scheduleTimeTextSize: Int,
    @Sp
    private val scheduleNumberTextSize: Int,
    @Sp
    private val headerMonthTextSize: Int,
    @Sp
    private val headerMonthDateTextSize: Int,
    @Sp
    private val headerWeekDayTextSize: Int,
) {
    companion object {
        @Sp
        private fun getOffset(context: Context) = context.resources.getInteger(R.integer.schedule_text_size_offset)
    }

    @Sp
    fun getCourseTextSize(context: Context) = courseTextSize + getOffset(context).toFloat()

    @Sp
    fun getScheduleTimeTextSize(context: Context) = scheduleTimeTextSize + getOffset(context).toFloat()

    @Sp
    fun getScheduleNumberTextSize(context: Context) = scheduleNumberTextSize + getOffset(context).toFloat()

    @Sp
    fun getHeaderMonthTextSize(context: Context) = headerMonthTextSize + getOffset(context).toFloat()

    @Sp
    fun getHeaderMonthDateTextSize(context: Context) = headerMonthDateTextSize + getOffset(context).toFloat()

    @Sp
    fun getHeaderWeekdayTextSize(context: Context) = headerWeekDayTextSize + getOffset(context).toFloat()
}