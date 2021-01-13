package tool.xfy9326.schedule.data

import androidx.datastore.preferences.core.remove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import tool.xfy9326.schedule.beans.ImageScareType
import tool.xfy9326.schedule.beans.NotThisWeekCourseShowStyle
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.kt.asParentOf
import tool.xfy9326.schedule.kt.tryEnumValueOf
import tool.xfy9326.schedule.utils.DirUtils
import java.io.File

object ScheduleDataStore : AbstractDataStore("ScheduleSettings") {
    private val firstDayOfWeek by preferencesKey<String>()
    private val scheduleViewAlpha by preferencesKey<Int>()
    private val forceShowWeekendColumn by preferencesKey<Boolean>()
    private val showNotThisWeekCourse by preferencesKey<Boolean>()
    private val customScheduleTextColor by preferencesKey<Boolean>()
    val timeTextColor by preferencesKey<Int>()
    val toolBarTintColor by preferencesKey<Int>()
    private val courseCellTextSize by preferencesKey<Int>()
    private val cornerScreenMargin by preferencesKey<Boolean>()
    private val highlightShowTodayCell by preferencesKey<Boolean>()
    val highlightShowTodayCellColor by preferencesKey<Int>()
    private val scheduleBackgroundImage by preferencesKey<String>()
    private val scheduleBackgroundImageQuality by preferencesKey<Int>()
    private val scheduleBackgroundImageAlpha by preferencesKey<Int>()
    private val enableScheduleBackground by preferencesKey<Boolean>()
    private val scheduleBackgroundScareType by preferencesKey<String>()
    private val useLightColorStatusBarColor by preferencesKey<Boolean>()
    private val scheduleBackgroundFadeAnim by preferencesKey<Boolean>()
    private val showScheduleTimes by preferencesKey<Boolean>()
    private val horizontalCourseCellText by preferencesKey<Boolean>()
    private val verticalCourseCellText by preferencesKey<Boolean>()
    val notThisWeekCourseShowStyle by preferencesSetKey<String>()

    val firstDayOfWeekFlow = firstDayOfWeek.readEnumAsFlow(WeekDay.MONDAY)

    val scheduleStylesFlow = readOnlyFlow.combine(firstDayOfWeekFlow) { pref, weekday ->
        ScheduleStyles(
            firstDayOfWeek = weekday,
            viewAlpha = pref[scheduleViewAlpha] ?: 100,
            forceShowWeekendColumn = pref[forceShowWeekendColumn] ?: false,
            showNotThisWeekCourse = pref[showNotThisWeekCourse] ?: true,
            timeTextColor = if (pref[customScheduleTextColor] == true) pref[timeTextColor] else null,
            courseCellTextSize = pref[courseCellTextSize] ?: 3,
            cornerScreenMargin = pref[cornerScreenMargin] ?: false,
            highlightShowTodayCell = pref[highlightShowTodayCell] ?: true,
            highlightShowTodayCellColor = if (pref[customScheduleTextColor] == true) pref[highlightShowTodayCellColor] else null,
            showScheduleTimes = pref[showScheduleTimes] ?: true,
            horizontalCourseCellText = pref[horizontalCourseCellText] ?: false,
            verticalCourseCellText = pref[verticalCourseCellText] ?: false,
            notThisWeekCourseShowStyle = tryEnumValueOf(pref[notThisWeekCourseShowStyle])
                ?: setOf(NotThisWeekCourseShowStyle.USE_TRANSPARENT_BACKGROUND)
        )
    }.distinctUntilChanged()

    suspend fun setScheduleBackgroundImage(fileName: String?) = edit {
        if (fileName == null) {
            it.remove(scheduleBackgroundImage)
        } else {
            it[scheduleBackgroundImage] = fileName
        }
    }

    val useLightColorStatusBarColorFlow = useLightColorStatusBarColor.readAsFlow(false)

    val toolBarTintColorFlow = read {
        if (it[customScheduleTextColor] == true) it[toolBarTintColor] else null
    }

    val scheduleBackgroundImageQualityFlow = scheduleBackgroundImageQuality.readAsFlow(60)

    val scheduleBackgroundImageFlow = scheduleBackgroundImage.readAsFlow()

    val scheduleBackgroundBuildBundleFlow = read {
        val enabled = it[enableScheduleBackground] ?: false
        val fileName = it[scheduleBackgroundImage]
        if (enabled && fileName != null) {
            it to fileName
        } else {
            null
        }
    }.flowOn(Dispatchers.IO).map {
        if (it != null) {
            val file = DirUtils.PictureAppDir.asParentOf(it.second)
            if (file.isFile) {
                it.first to file
            } else {
                setScheduleBackgroundImage(null)
                null
            }
        } else {
            null
        }
    }.map {
        if (it != null) {
            ScheduleBackgroundBuildBundle(
                file = it.second,
                scareType = tryEnumValueOf<ImageScareType>(it.first[scheduleBackgroundScareType]) ?: ImageScareType.CENTER_CROP,
                alpha = (it.first[scheduleBackgroundImageAlpha] ?: 100) / 100f,
                fadeAnim = it.first[scheduleBackgroundFadeAnim] ?: true
            )
        } else {
            null
        }
    }.conflate()

    data class ScheduleBackgroundBuildBundle(
        val file: File,
        val scareType: ImageScareType,
        val alpha: Float,
        val fadeAnim: Boolean,
    )
}