package tool.xfy9326.schedule.data

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import lib.xfy9326.android.kit.io.IOManager
import lib.xfy9326.kit.tryEnumValueOf
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.io.FileManager
import java.io.File

object ScheduleDataStore : AbstractDataStore("ScheduleSettings") {
    private val defaultFirstDayOfWeek by stringPreferencesKey()
    private val scheduleViewAlpha by intPreferencesKey()
    private val forceShowWeekendColumn by booleanPreferencesKey()
    private val showNotThisWeekCourse by booleanPreferencesKey()
    private val customScheduleTextColor by booleanPreferencesKey()
    private val cornerScreenMargin by booleanPreferencesKey()
    private val highlightShowTodayCell by booleanPreferencesKey()
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
    private val notThisWeekCourseCellAlpha by intPreferencesKey()
    private val courseCellTextNoChangeLine by booleanPreferencesKey()
    private val showCourseCellLocation by booleanPreferencesKey()

    val courseCellVerticalPadding by intPreferencesKey()
    val courseCellHorizontalPadding by intPreferencesKey()

    val timeTextColor by intPreferencesKey()
    val toolBarTintColor by intPreferencesKey()
    val highlightShowTodayCellColor by intPreferencesKey()
    val notThisWeekCourseShowStyle by stringSetPreferencesKey()

    private val courseCellAutoHeight by booleanPreferencesKey()
    val courseCellHeight by intPreferencesKey()
    private val courseCellAutoTextLength by booleanPreferencesKey()
    val courseCellTextLength by intPreferencesKey()
    private val courseCellShowAllCourseText by booleanPreferencesKey()
    val courseCellCourseTextLength by intPreferencesKey()

    val courseTextSize by intPreferencesKey()
    val scheduleTimeTextSize by intPreferencesKey()
    val scheduleNumberTextSize by intPreferencesKey()
    val headerMonthTextSize by intPreferencesKey()
    val headerMonthDateTextSize by intPreferencesKey()
    val headerWeekDayTextSize by intPreferencesKey()

    suspend fun resetScheduleTextSize() {
        edit {
            for (value in ScheduleText.values()) {
                it.remove(value.prefKey)
            }
        }
    }

    suspend fun readScheduleTextSize(textType: ScheduleText) = read { ScheduleText.TextSize.create(it, textType) }.first()

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
            notThisWeekCourseShowStyle = tryEnumValueOf(it[notThisWeekCourseShowStyle]) ?: NotThisWeekCourseShowStyle.valueSet,
            enableScheduleGridScroll = it[enableScheduleGridScroll] ?: true,
            textSize = ScheduleText.TextSize.create(it),
            notThisWeekCourseCellAlpha = it[notThisWeekCourseCellAlpha] ?: IOManager.resources.getInteger(R.integer.default_schedule_not_this_week_course_alpha),
            courseCellHeight = if (it[courseCellAutoHeight] == false) getCourseCellHeightFromPref(it) else null,
            courseCellTextLength = if (it[courseCellAutoTextLength] == false) getCourseCellTextLengthFromPref(it) else null,
            courseCellTextNoChangeLine = it[courseCellTextNoChangeLine] ?: false,
            courseCellCourseTextLength = if (it[courseCellShowAllCourseText] == false) getCourseCellCourseTextLengthFromPref(it) else null,
            showCourseCellLocation = it[showCourseCellLocation] ?: true,
            courseCellVerticalPadding = getCourseCellVerticalPaddingFromPref(it),
            courseCellHorizontalPadding = getCourseCellHorizontalPaddingFromPref(it),
        )
    }.distinctUntilChanged()

    val courseCellHeightFlow = read { getCourseCellHeightFromPref(it) }

    val courseCellTextLengthFlow = read { getCourseCellTextLengthFromPref(it) }

    val courseCellCourseTextLengthFlow = read { getCourseCellCourseTextLengthFromPref(it) }

    val courseCellVerticalPaddingFlow = read { getCourseCellVerticalPaddingFromPref(it) }

    val courseCellHorizontalPaddingFlow = read { getCourseCellHorizontalPaddingFromPref(it) }

    private fun getCourseCellHeightFromPref(pref: Preferences): Int =
        pref[courseCellHeight] ?: IOManager.resources.getInteger(R.integer.default_schedule_course_cell_height)

    private fun getCourseCellTextLengthFromPref(pref: Preferences): Int =
        pref[courseCellTextLength] ?: IOManager.resources.getInteger(R.integer.default_schedule_course_cell_text_length)

    private fun getCourseCellCourseTextLengthFromPref(pref: Preferences): Int =
        pref[courseCellCourseTextLength] ?: IOManager.resources.getInteger(R.integer.default_schedule_course_cell_course_text_length)

    private fun getCourseCellVerticalPaddingFromPref(pref: Preferences): Int =
        pref[courseCellVerticalPadding] ?: IOManager.resources.getInteger(R.integer.default_schedule_course_cell_vertical_padding)

    private fun getCourseCellHorizontalPaddingFromPref(pref: Preferences): Int =
        pref[courseCellHorizontalPadding] ?: IOManager.resources.getInteger(R.integer.default_schedule_course_cell_horizontal_padding)

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
        if (it[customScheduleTextColor] == true) {
            it[toolBarTintColor]
        } else {
            null
        }
    }

    val scheduleBackgroundImageQualityFlow = scheduleBackgroundImageQuality.readAsFlowLazy {
        IOManager.resources.getInteger(R.integer.default_schedule_background_image_quality)
    }

    val scheduleBackgroundImageFlow = scheduleBackgroundImage.readAsFlow()

    val scheduleBackgroundBuildBundleFlow = read {
        val enabled = it[enableScheduleBackground] ?: false
        val fileName = it[scheduleBackgroundImage]
        if (enabled && fileName != null) {
            return@read ScheduleBackgroundBuildBundle(
                file = FileManager.getAppPictureFile(fileName),
                scaleType = tryEnumValueOf<ImageScaleType>(it[scheduleBackgroundScaleType]) ?: ImageScaleType.CENTER_CROP,
                viewAlpha = it[scheduleBackgroundImageAlpha] ?: IOManager.resources.getInteger(R.integer.default_schedule_background_image_alpha),
                useAnim = it[scheduleBackgroundUseAnim] ?: true
            )
        }
        null
    }.flowOn(Dispatchers.IO)

    data class ScheduleBackgroundBuildBundle(
        val file: File,
        val scaleType: ImageScaleType,
        private val viewAlpha: Int,
        val useAnim: Boolean,
    ) {
        val alpha = viewAlpha / 100f
    }
}