package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.io.IOManager
import tool.xfy9326.schedule.kt.AppInstance
import tool.xfy9326.schedule.kt.getColorCompat

class SchedulePredefine private constructor(
    @Px
    val gridCellPadding: Int,
    @Px
    val timeCellVerticalPadding: Int,
    @Px
    val timeCellTimeDivideTopMargin: Int,
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
        private const val DEFAULT_NOT_THIS_WEEK_COURSE_ALPHA = 0.5f

        val content by lazy(LazyThreadSafetyMode.NONE) {
            SchedulePredefine(
                gridCellPadding = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_grid_cell_padding),
                timeCellVerticalPadding = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_time_cell_vertical_padding),
                timeCellTimeDivideTopMargin = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_time_divide_top_margin),
                gridBottomCornerScreenMargin = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_grid_bottom_corner_screen_margin),
                courseCellBackgroundRadius = IOManager.resources.getDimension(R.dimen.schedule_grid_cell_radius),
                courseCellTextPadding = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_course_cell_text_padding),
                courseCellTextColorLight = AppInstance.getColorCompat(R.color.course_cell_text_light),
                courseCellTextColorDark = AppInstance.getColorCompat(R.color.course_cell_text_dark),
                notThisWeekCourseCellAlpha = DEFAULT_NOT_THIS_WEEK_COURSE_ALPHA,
                courseCellRippleColor = AppInstance.getColorCompat(R.color.course_cell_ripple)
            )
        }
    }
}