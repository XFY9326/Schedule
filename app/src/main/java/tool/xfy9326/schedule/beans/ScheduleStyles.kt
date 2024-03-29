package tool.xfy9326.schedule.beans

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.github.xfy9326.atools.ui.dpToPx
import io.github.xfy9326.atools.ui.getColorCompat
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.annotation.Dp
import tool.xfy9326.schedule.utils.getBottomScreenCornerMargin
import tool.xfy9326.schedule.utils.getSystemBarBottomInsets
import kotlin.math.max

data class ScheduleStyles(
    private val viewAlpha: Int,
    val forceShowWeekendColumn: Boolean,
    val showNotThisWeekCourse: Boolean,
    @ColorInt
    private val timeTextColor: Int?, // null -> default
    val cornerScreenMargin: Boolean,
    val highlightShowTodayCell: Boolean,
    @ColorInt
    private val highlightShowTodayCellColor: Int?, // null -> default
    val showScheduleTimes: Boolean,
    val horizontalCourseCellText: Boolean,
    val verticalCourseCellText: Boolean,
    val notThisWeekCourseShowStyle: Set<NotThisWeekCourseShowStyle>,
    val enableScheduleGridScroll: Boolean,
    val textSize: ScheduleText.TextSize,
    private val notThisWeekCourseCellAlpha: Int,
    @Dp
    private val courseCellHeight: Int?, // null -> auto
    val courseCellTextLength: Int?, // null -> auto,
    val courseCellTextNoNewLine: Boolean,
    val courseCellCourseTextLength: Int?, // null -> auto,
    @Px
    val courseCellVerticalPadding: Int,
    @Px
    val courseCellHorizontalPadding: Int,
    val courseCellDetailContent: Set<CourseCellDetailContent>,
    val courseCellFullScreenSameHeight: Boolean,
    val courseCellFullScreenWithBottomInsets: Boolean
) {
    val scheduleViewAlpha = viewAlpha / 100f

    @Px
    val rowHeight = courseCellHeight?.dpToPx()

    val notThisWeekCourseAlpha = notThisWeekCourseCellAlpha / 100f

    fun getTimeTextColor(context: Context) = timeTextColor ?: context.getColorCompat(R.color.course_time_cell_text)

    fun getHighlightShowTodayCellColor(context: Context) = highlightShowTodayCellColor ?: context.getColorCompat(R.color.course_time_today_highlight)

    fun getBottomSpaceHeight(view: View, defaultBottomCornerSize: Int): Int =
        if (cornerScreenMargin) {
            max(view.getBottomScreenCornerMargin() ?: defaultBottomCornerSize, view.getSystemBarBottomInsets())
        } else {
            view.getSystemBarBottomInsets()
        }
}