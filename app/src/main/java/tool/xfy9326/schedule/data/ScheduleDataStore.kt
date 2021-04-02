package tool.xfy9326.schedule.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tool.xfy9326.schedule.beans.ImageScareType
import tool.xfy9326.schedule.beans.NotThisWeekCourseShowStyle
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.io.FileManager
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
    private val courseCellTextSize by intPreferencesKey()
    private val cornerScreenMargin by booleanPreferencesKey()
    private val highlightShowTodayCell by booleanPreferencesKey()
    val highlightShowTodayCellColor by intPreferencesKey()
    private val scheduleBackgroundImage by stringPreferencesKey()
    private val scheduleBackgroundImageQuality by intPreferencesKey()
    private val scheduleBackgroundImageAlpha by intPreferencesKey()
    private val enableScheduleBackground by booleanPreferencesKey()
    private val scheduleBackgroundScareType by stringPreferencesKey()
    private val useLightColorSystemBarColor by booleanPreferencesKey()
    private val scheduleBackgroundUseAnim by booleanPreferencesKey()
    private val showScheduleTimes by booleanPreferencesKey()
    private val horizontalCourseCellText by booleanPreferencesKey()
    private val verticalCourseCellText by booleanPreferencesKey()
    private val enableScheduleGridScroll by booleanPreferencesKey()
    val notThisWeekCourseShowStyle by stringSetPreferencesKey()

    val defaultFirstDayOfWeekFlow = defaultFirstDayOfWeek.readEnumAsFlow(WeekDay.MONDAY)

    val scheduleStylesFlow = read {
        ScheduleStyles(
            viewAlpha = it[scheduleViewAlpha] ?: 100,
            forceShowWeekendColumn = it[forceShowWeekendColumn] ?: false,
            showNotThisWeekCourse = it[showNotThisWeekCourse] ?: true,
            timeTextColor = if (it[customScheduleTextColor] == true) it[timeTextColor] else null,
            courseCellTextSize = it[courseCellTextSize] ?: 3,
            cornerScreenMargin = it[cornerScreenMargin] ?: false,
            highlightShowTodayCell = it[highlightShowTodayCell] ?: true,
            highlightShowTodayCellColor = if (it[customScheduleTextColor] == true) it[highlightShowTodayCellColor] else null,
            showScheduleTimes = it[showScheduleTimes] ?: true,
            horizontalCourseCellText = it[horizontalCourseCellText] ?: false,
            verticalCourseCellText = it[verticalCourseCellText] ?: false,
            notThisWeekCourseShowStyle = tryEnumValueOf(it[notThisWeekCourseShowStyle])
                ?: setOf(NotThisWeekCourseShowStyle.USE_TRANSPARENT_BACKGROUND),
            enableScheduleGridScroll = it[enableScheduleGridScroll] ?: true
        )
    }.distinctUntilChanged()

    suspend fun setScheduleBackgroundImage(fileName: String?) = edit {
        if (fileName == null) {
            it.remove(scheduleBackgroundImage)
        } else {
            it[scheduleBackgroundImage] = fileName
        }
    }

    val useLightColorSystemBarColorFlow = useLightColorSystemBarColor.readAsFlow(false)

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
                scareType = tryEnumValueOf<ImageScareType>(it[scheduleBackgroundScareType]) ?: ImageScareType.CENTER_CROP,
                alpha = (it[scheduleBackgroundImageAlpha] ?: 100) / 100f,
                useAnim = it[scheduleBackgroundUseAnim] ?: true
            )
        }
        null
    }.flowOn(Dispatchers.IO)

    data class ScheduleBackgroundBuildBundle(
        val file: File,
        val scareType: ImageScareType,
        val alpha: Float,
        val useAnim: Boolean,
    )
}