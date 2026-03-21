package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt
import androidx.annotation.Px
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.tools.core.AppContext
import tool.xfy9326.schedule.tools.io.IOManager
import tool.xfy9326.schedule.tools.ui.getColorCompat

class SchedulePredefine private constructor(
    @param:Px
    val timeCellVerticalPadding: Int,
    @param:Px
    val timeCellTimeDivideTopMargin: Int,
    @param:Px
    val gridBottomCornerScreenMargin: Int,
    @param:Px
    val courseCellBackgroundRadius: Float,
    @param:Px
    val courseCellTextPadding: Int,
    @param:ColorInt
    val courseCellTextColorLight: Int,
    @param:ColorInt
    val courseCellTextColorDark: Int,
    @param:ColorInt
    val courseCellRippleColor: Int,
) {
    companion object {
        val content by lazy(LazyThreadSafetyMode.NONE) {
            SchedulePredefine(
                timeCellVerticalPadding = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_time_cell_vertical_padding),
                timeCellTimeDivideTopMargin = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_time_divide_top_margin),
                gridBottomCornerScreenMargin = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_grid_bottom_corner_screen_margin),
                courseCellBackgroundRadius = IOManager.resources.getDimension(R.dimen.schedule_grid_cell_radius),
                courseCellTextPadding = IOManager.resources.getDimensionPixelSize(R.dimen.schedule_course_cell_text_padding),
                courseCellTextColorLight = AppContext.getColorCompat(R.color.course_cell_text_light),
                courseCellTextColorDark = AppContext.getColorCompat(R.color.course_cell_text_dark),
                courseCellRippleColor = AppContext.getColorCompat(R.color.course_cell_ripple)
            )
        }
    }
}