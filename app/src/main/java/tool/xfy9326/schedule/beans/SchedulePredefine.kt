package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.github.xfy9326.atools.core.AppContext
import io.github.xfy9326.atools.io.IOManager
import io.github.xfy9326.atools.ui.getColorCompat
import tool.xfy9326.schedule.R

class SchedulePredefine private constructor(
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
    @ColorInt
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