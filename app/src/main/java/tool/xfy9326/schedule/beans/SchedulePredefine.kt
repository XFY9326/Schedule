package tool.xfy9326.schedule.beans

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.getColorCompat

class SchedulePredefine(
    @Px
    val monthTextSize: Float,
    @Px
    val weekDayTextSize: Float,
    @Px
    val monthDateTextSize: Float,
    @Px
    val gridCellPadding: Int,
    @Px
    val timeCellVerticalPadding: Int,
    @Px
    val timeCellTimeDivideTopMargin: Int,
    @Px
    val timeCellScheduleTimeTextSize: Float,
    @Px
    val timeCellCourseNumTextSize: Float,
    @Px
    val gridBottomCornerScreenMargin: Int,
    @Px
    val courseCellBackgroundRadius: Float,
    @Px
    val courseCellTextPadding: Int,
    @ColorInt
    val courseCellTextColorLight: Int,
    @ColorInt
    val courseCellTextColorDark: Int,
    @FloatRange(from = 0.0, to = 1.0)
    val notThisWeekCourseCellAlpha: Float,
    @ColorInt
    val courseCellRippleColor: Int,
) {
    companion object {
        fun load(context: Context) = context.let {
            SchedulePredefine(
                monthTextSize = it.resources.getDimension(R.dimen.schedule_header_month_text_size),
                weekDayTextSize = it.resources.getDimension(R.dimen.schedule_header_weekday_text_size),
                monthDateTextSize = it.resources.getDimension(R.dimen.schedule_header_month_date_text_size),
                gridCellPadding = it.resources.getDimensionPixelSize(R.dimen.schedule_grid_cell_padding),
                timeCellVerticalPadding = it.resources.getDimensionPixelSize(R.dimen.schedule_time_cell_vertical_padding),
                timeCellTimeDivideTopMargin = it.resources.getDimensionPixelSize(R.dimen.schedule_time_divide_top_margin),
                timeCellScheduleTimeTextSize = it.resources.getDimension(R.dimen.schedule_time_text_size),
                timeCellCourseNumTextSize = it.resources.getDimension(R.dimen.schedule_time_cell_course_num_text_size),
                gridBottomCornerScreenMargin = context.resources.getDimensionPixelSize(R.dimen.schedule_grid_bottom_corner_screen_margin),
                courseCellBackgroundRadius = context.resources.getDimension(R.dimen.schedule_grid_cell_radius),
                courseCellTextPadding = context.resources.getDimensionPixelSize(R.dimen.schedule_course_cell_text_padding),
                courseCellTextColorLight = context.getColorCompat(R.color.course_cell_text_light),
                courseCellTextColorDark = context.getColorCompat(R.color.course_cell_text_dark),
                notThisWeekCourseCellAlpha = 0.5f,
                courseCellRippleColor = context.getColorCompat(R.color.course_cell_ripple)
            )
        }
    }
}