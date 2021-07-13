package tool.xfy9326.schedule.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.io.FileManager
import tool.xfy9326.schedule.io.IOManager
import tool.xfy9326.schedule.kt.tryEnumValueOf
import java.io.File

object ScheduleDataStore : AbstractDataStore("ScheduleSettings") {
    private val defaultFirstDayOfWeek by stringPreferencesKey()
    private val scheduleViewAlpha by intPreferencesKey()
    private val forceShowWeekendColumn by booleanPreferencesKey()
    private val showNotThisWeekCourse by booleanPreferencesKey()
    private val customScheduleTextColor by booleanPreferencesKey()
    val timeTextColor by intPreferencesKey()
    val toolBarTintColor by intPreferencesKey()
    private val cornerScreenMargin by booleanPreferencesKey()
    private val highlightShowTodayCell by booleanPreferencesKey()
    val highlightShowTodayCellColor by intPreferencesKey()
    private val scheduleBackgroundImage by stringPreferencesKey()
    private val scheduleBackgroundImageQuality by intPreferencesKey()
    private val scheduleBackgroundImageAlpha by intPreferencesKey()
    private val enableScheduleBackground by booleanPreferencesKey()
    private val scheduleBackgroundScaleType by stringPreferencesKey()
    private val scheduleSystemBarAppearance by stringPreferencesKey()
    private val scheduleBackgroundUseAnim by booleanPreferencesKey()
    private val showScheduleTimes by booleanPreferencesKey()
    private val horizontalCourseCellText by booleanPreferencesKey()
    private val verticalCourseCellText by booleanPreferencesKey()
    private val enableScheduleGridScroll by booleanPreferencesKey()
    val notThisWeekCourseShowStyle by stringSetPreferencesKey()

    val courseTextSize by intPreferencesKey()
    val scheduleTimeTextSize by intPreferencesKey()
    val scheduleNumberTextSize by intPreferencesKey()
    val headerMonthTextSize by intPreferencesKey()
    val headerMonthDateTextSize by intPreferencesKey()
    val headerWeekDayTextSize by intPreferencesKey()

    suspend fun resetScheduleTextSize() {
        edit {
            it.remove(courseTextSize)
            it.remove(scheduleTimeTextSize)
            it.remove(scheduleNumberTextSize)
            it.remove(headerMonthTextSize)
            it.remove(headerMonthDateTextSize)
            it.remove(headerWeekDayTextSize)
        }
    }

    val scheduleStylesFlow = read {
        ScheduleStyles(
            viewAlpha = it[scheduleViewAlpha] ?: 100,
            forceShowWeekendColumn = it[forceShowWeekendColumn] ?: false,
            showNotThisWeekCourse = it[showNotThisWeekCourse] ?: true,
            timeTextColor = if (it[customScheduleTextColor] == true) it[timeTextColor] else null,
            cornerScreenMargin = it[cornerScreenMargin] ?: false,
            highlightShowTodayCell = it[highlightShowTodayCell] ?: true,
            highlightShowTodayCellColor = if (it[customScheduleTextColor] == true) it[highlightShowTodayCellColor] else null,
            showScheduleTimes = it[showScheduleTimes] ?: true,
            horizontalCourseCellText = it[horizontalCourseCellText] ?: false,
            verticalCourseCellText = it[verticalCourseCellText] ?: false,
            notThisWeekCourseShowStyle = tryEnumValueOf(it[notThisWeekCourseShowStyle])
                ?: setOf(NotThisWeekCourseShowStyle.SHOW_NOT_THIS_WEEK_TEXT, NotThisWeekCourseShowStyle.USE_TRANSPARENT_BACKGROUND),
            enableScheduleGridScroll = it[enableScheduleGridScroll] ?: true,
            textSize = ScheduleTextSize(
                courseTextSize = it[courseTextSize] ?: IOManager.resources.getInteger(R.integer.schedule_course_default_text_size),
                scheduleTimeTextSize = it[scheduleTimeTextSize] ?: IOManager.resources.getInteger(R.integer.schedule_time_default_text_size),
                scheduleNumberTextSize = it[scheduleNumberTextSize] ?: IOManager.resources.getInteger(R.integer.schedule_number_default_text_size),
                headerMonthTextSize = it[headerMonthTextSize] ?: IOManager.resources.getInteger(R.integer.schedule_header_month_default_text_size),
                headerMonthDateTextSize = it[headerMonthDateTextSize] ?: IOManager.resources.getInteger(R.integer.schedule_header_month_date_default_text_size),
                headerWeekDayTextSize = it[headerWeekDayTextSize] ?: IOManager.resources.getInteger(R.integer.schedule_header_weekday_default_text_size)
            )
        )
    }.distinctUntilChanged()

    val defaultFirstDayOfWeekFlow = defaultFirstDayOfWeek.readEnumAsFlow(WeekDay.MONDAY)

    suspend fun setScheduleBackgroundImage(fileName: String?) = edit {
        if (fileName == null) {
            it.remove(scheduleBackgroundImage)
        } else {
            it[scheduleBackgroundImage] = fileName
        }
    }

    val scheduleSystemBarAppearanceFlow = scheduleSystemBarAppearance.readEnumAsFlow(SystemBarAppearance.FOLLOW_THEME)

    val toolBarTintColorFlow = read {
        if (it[customScheduleTextColor] == true) it[toolBarTintColor] else null
    }

    val scheduleBackgroundImageQualityFlow = scheduleBackgroundImageQuality.readAsFlow(60)

    val scheduleBackgroundImageFlow = scheduleBackgroundImage.readAsFlow()

    val scheduleBackgroundBuildBundleFlow = read {
        val enabled = it[enableScheduleBackground] ?: false
        val fileName = it[scheduleBackgroundImage]
        if (enabled && fileName != null) {
            return@read ScheduleBackgroundBuildBundle(
                file = FileManager.getAppPictureFile(fileName),
                scaleType = tryEnumValueOf<ImageScaleType>(it[scheduleBackgroundScaleType]) ?: ImageScaleType.CENTER_CROP,
                alpha = (it[scheduleBackgroundImageAlpha] ?: 100) / 100f,
                useAnim = it[scheduleBackgroundUseAnim] ?: true
            )
        }
        null
    }.flowOn(Dispatchers.IO)

    data class ScheduleBackgroundBuildBundle(
        val file: File,
        val scaleType: ImageScaleType,
        val alpha: Float,
        val useAnim: Boolean,
    )
}