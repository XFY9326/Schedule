package tool.xfy9326.schedule.data

import androidx.datastore.preferences.core.Preferences
import io.github.xfy9326.atools.base.tryEnumSetValueOf
import io.github.xfy9326.atools.base.tryEnumValueOf
import io.github.xfy9326.atools.datastore.preference.booleanPrefKey
import io.github.xfy9326.atools.datastore.preference.intPrefKey
import io.github.xfy9326.atools.datastore.preference.stringPrefKey
import io.github.xfy9326.atools.datastore.preference.stringSetPrefKey
import io.github.xfy9326.atools.io.IOManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseCellDetailContent
import tool.xfy9326.schedule.beans.ImageScaleType
import tool.xfy9326.schedule.beans.NotThisWeekCourseShowStyle
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.ScheduleText
import tool.xfy9326.schedule.beans.SystemBarAppearance
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.io.FileManager
import java.io.File

object ScheduleDataStore : AbstractDataStore("ScheduleSettings") {
    private val defaultFirstDayOfWeek by stringPrefKey()
    private val scheduleViewAlpha by intPrefKey()
    private val forceShowWeekendColumn by booleanPrefKey()
    private val showNotThisWeekCourse by booleanPrefKey()
    private val customScheduleTextColor by booleanPrefKey()
    private val cornerScreenMargin by booleanPrefKey()
    private val highlightShowTodayCell by booleanPrefKey()
    private val scheduleBackgroundImage by stringPrefKey()
    private val scheduleBackgroundImageQuality by intPrefKey()
    private val scheduleBackgroundImageAlpha by intPrefKey()
    private val enableScheduleBackground by booleanPrefKey()
    private val scheduleBackgroundScaleType by stringPrefKey()
    private val scheduleSystemBarAppearance by stringPrefKey()
    private val scheduleBackgroundUseAnim by booleanPrefKey()
    private val showScheduleTimes by booleanPrefKey()
    private val horizontalCourseCellText by booleanPrefKey()
    private val verticalCourseCellText by booleanPrefKey()
    private val enableScheduleGridScroll by booleanPrefKey()
    private val notThisWeekCourseCellAlpha by intPrefKey()
    private val courseCellTextNoNewLine by booleanPrefKey()
    private val courseCellDetailContent by stringSetPrefKey()
    private val courseCellFullScreenSameHeight by booleanPrefKey()
    private val courseCellFullScreenWithBottomInsets by booleanPrefKey()

    val courseCellVerticalPadding by intPrefKey()
    val courseCellHorizontalPadding by intPrefKey()

    val timeTextColor by intPrefKey()
    val toolBarTintColor by intPrefKey()
    val highlightShowTodayCellColor by intPrefKey()
    val notThisWeekCourseShowStyle by stringSetPrefKey()

    private val courseCellAutoHeight by booleanPrefKey()
    val courseCellHeight by intPrefKey()
    private val courseCellAutoTextLength by booleanPrefKey()
    val courseCellTextLength by intPrefKey()
    private val courseCellShowAllCourseText by booleanPrefKey()
    val courseCellCourseTextLength by intPrefKey()

    val courseTextSize by intPrefKey()
    val scheduleTimeTextSize by intPrefKey()
    val scheduleNumberTextSize by intPrefKey()
    val headerMonthTextSize by intPrefKey()
    val headerMonthDateTextSize by intPrefKey()
    val headerWeekDayTextSize by intPrefKey()

    suspend fun resetScheduleTextSize() {
        edit {
            for (value in ScheduleText.values()) {
                it.remove(value.prefKey)
            }
        }
    }

    suspend fun readScheduleTextSize(textType: ScheduleText) = read { ScheduleText.TextSize.create(it, textType) }.first()

    private fun Preferences.getCourseCellFullScreenSameHeight() =
        if (this[courseCellAutoHeight] == false) this[courseCellFullScreenSameHeight] ?: false else false

    private fun Preferences.courseCellFullScreenWithBottomInsets() =
        if (getCourseCellFullScreenSameHeight()) this[courseCellFullScreenWithBottomInsets] ?: false else false

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
            notThisWeekCourseShowStyle = it[notThisWeekCourseShowStyle]?.let { value -> tryEnumSetValueOf(value) }
                ?: NotThisWeekCourseShowStyle.valueSet,
            enableScheduleGridScroll = it[enableScheduleGridScroll] ?: true,
            textSize = ScheduleText.TextSize.create(it),
            notThisWeekCourseCellAlpha = it[notThisWeekCourseCellAlpha]
                ?: IOManager.resources.getInteger(R.integer.default_schedule_not_this_week_course_alpha),
            courseCellHeight = if (it[courseCellAutoHeight] == false) getCourseCellHeightFromPref(it) else null,
            courseCellTextLength = if (it[courseCellAutoTextLength] == false) getCourseCellTextLengthFromPref(it) else null,
            courseCellTextNoNewLine = it[courseCellTextNoNewLine] ?: false,
            courseCellCourseTextLength = if (it[courseCellShowAllCourseText] == false) getCourseCellCourseTextLengthFromPref(it) else null,
            courseCellVerticalPadding = getCourseCellVerticalPaddingFromPref(it),
            courseCellHorizontalPadding = getCourseCellHorizontalPaddingFromPref(it),
            courseCellDetailContent = it[courseCellDetailContent]?.let { value -> tryEnumSetValueOf(value) }
                ?: setOf(CourseCellDetailContent.LOCATION),
            courseCellFullScreenSameHeight = it.getCourseCellFullScreenSameHeight(),
            courseCellFullScreenWithBottomInsets = it.courseCellFullScreenWithBottomInsets()
        )
    }.distinctUntilChanged()

    val courseCellAutoLengthFlow = read { it[courseCellAutoHeight] ?: true }

    val courseCellFullScreenSameHeightFlow = read { it.getCourseCellFullScreenSameHeight() }

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
                scaleType = it[scheduleBackgroundScaleType]?.let { value -> tryEnumValueOf<ImageScaleType>(value) } ?: ImageScaleType.CENTER_CROP,
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