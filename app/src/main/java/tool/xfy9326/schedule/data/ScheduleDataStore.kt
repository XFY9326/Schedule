package tool.xfy9326.schedule.data

import androidx.datastore.preferences.core.Preferences
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
import tool.xfy9326.schedule.tools.base.tryEnumSetValueOf
import tool.xfy9326.schedule.tools.base.tryEnumValueOf
import tool.xfy9326.schedule.tools.datastore.preference.key.booleanKey
import tool.xfy9326.schedule.tools.datastore.preference.key.intKey
import tool.xfy9326.schedule.tools.datastore.preference.key.stringKey
import tool.xfy9326.schedule.tools.datastore.preference.key.stringSetKey
import tool.xfy9326.schedule.tools.io.IOManager
import java.io.File

object ScheduleDataStore : AbstractDataStore("ScheduleSettings") {
    private val defaultFirstDayOfWeek by stringKey()
    private val scheduleViewAlpha by intKey()
    private val forceShowWeekendColumn by booleanKey()
    private val showNotThisWeekCourse by booleanKey()
    private val customScheduleTextColor by booleanKey()
    private val cornerScreenMargin by booleanKey()
    private val highlightShowTodayCell by booleanKey()
    private val scheduleBackgroundImage by stringKey()
    private val scheduleBackgroundImageQuality by intKey()
    private val scheduleBackgroundImageAlpha by intKey()
    private val enableScheduleBackground by booleanKey()
    private val scheduleBackgroundScaleType by stringKey()
    private val scheduleSystemBarAppearance by stringKey()
    private val scheduleBackgroundUseAnim by booleanKey()
    private val showScheduleTimes by booleanKey()
    private val horizontalCourseCellText by booleanKey()
    private val verticalCourseCellText by booleanKey()
    private val enableScheduleGridScroll by booleanKey()
    private val notThisWeekCourseCellAlpha by intKey()
    private val courseCellTextNoNewLine by booleanKey()
    private val courseCellDetailContent by stringSetKey()
    private val courseCellFullScreenSameHeight by booleanKey()
    private val courseCellFullScreenWithBottomInsets by booleanKey()

    val courseCellVerticalPadding by intKey()
    val courseCellHorizontalPadding by intKey()

    val timeTextColor by intKey()
    val toolBarTintColor by intKey()
    val highlightShowTodayCellColor by intKey()
    val notThisWeekCourseShowStyle by stringSetKey()

    private val courseCellAutoHeight by booleanKey()
    val courseCellHeight by intKey()
    private val courseCellAutoTextLength by booleanKey()
    val courseCellTextLength by intKey()
    private val courseCellShowAllCourseText by booleanKey()
    val courseCellCourseTextLength by intKey()

    val courseTextSize by intKey()
    val scheduleTimeTextSize by intKey()
    val scheduleNumberTextSize by intKey()
    val headerMonthTextSize by intKey()
    val headerMonthDateTextSize by intKey()
    val headerWeekDayTextSize by intKey()

    suspend fun resetScheduleTextSize() {
        edit {
            for (value in ScheduleText.entries) {
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