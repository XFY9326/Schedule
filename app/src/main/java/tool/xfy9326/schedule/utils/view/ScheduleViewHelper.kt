package tool.xfy9326.schedule.utils.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.core.graphics.applyCanvas
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import lib.xfy9326.android.kit.ApplicationInstance
import lib.xfy9326.android.kit.getColorCompat
import lib.xfy9326.android.kit.io.IOManager
import lib.xfy9326.android.kit.textBaselineHeight
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.view.*
import tool.xfy9326.schedule.ui.view.schedule.*
import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import tool.xfy9326.schedule.utils.schedule.CourseUtils
import java.util.*
import kotlin.collections.ArrayList

object ScheduleViewHelper {
    private const val MIN_SCHEDULE_COLUMN_COUNT = 5 + 1
    private const val MAX_SCHEDULE_COLUMN_COUNT = 7 + 1

    private val SAMPLE_SCHEDULE_TIMES by lazy {
        ScheduleTime.listOf(
            8, 0, 8, 40,
            8, 50, 9, 30,
            9, 50, 10, 30,
            10, 40, 11, 20,
            11, 30, 12, 10,
            13, 30, 14, 10,
            14, 20, 15, 0,
            15, 20, 16, 0,
            16, 10, 16, 50,
            17, 0, 17, 40
        )
    }
    private val SAMPLE_COURSE_CELLS by lazy {
        val colors = MaterialColorHelper.all()
        val sampleName = IOManager.resources.getString(R.string.sample_course_name)
        val sampleLocation = IOManager.resources.getString(R.string.sample_course_location)
        listOf(
            CourseCell(0, 0, sampleName, sampleLocation, SectionTime(WeekDay.MONDAY, 1, 2), colors[0], true),
            CourseCell(0, 1, sampleName, sampleLocation, SectionTime(WeekDay.MONDAY, 5, 2), colors[2], true),
            CourseCell(0, 2, sampleName, sampleLocation, SectionTime(WeekDay.TUESDAY, 5, 4), colors[4], true),
            CourseCell(0, 3, sampleName, sampleLocation, SectionTime(WeekDay.WEDNESDAY, 2, 2), colors[6], true),
            CourseCell(0, 4, sampleName, sampleLocation, SectionTime(WeekDay.WEDNESDAY, 5, 2), colors[8], false),
            CourseCell(0, 5, sampleName, sampleLocation, SectionTime(WeekDay.FRIDAY, 4, 3), colors[10], false),
            CourseCell(0, 6, sampleName, sampleLocation, SectionTime(WeekDay.FRIDAY, 8, 2), colors[12], true),
        )
    }

    suspend fun buildScheduleView(
        context: Context,
        viewData: ScheduleViewData,
        listener: ((CourseCell) -> Unit)? = null,
        noScroll: Boolean = false,
    ): View = withContext(Dispatchers.Default + SupervisorJob() + CoroutineName("schedule-view-builder-${viewData.weekNum}")) {
        val schedulePredefine = SchedulePredefine.content
        val showWeekend = viewData.styles.forceShowWeekendColumn || viewData.hasWeekendCourse

        val cellsDeferred = ArrayList<Deferred<ScheduleCellView>>(viewData.times.size + viewData.cells.size)

        for ((i, time) in viewData.times.withIndex()) {
            cellsDeferred.add(async { ScheduleCellView(context, i, time, schedulePredefine, viewData.styles) })
        }

        for (cell in viewData.cells) {
            if (cell.isThisWeekCourse || viewData.styles.showNotThisWeekCourse) {
                cellsDeferred.add(async { ScheduleCellView(context, showWeekend, cell, schedulePredefine, viewData.styles, viewData.weekStart) })
            }
        }

        val scheduleHeaderViewDeferred = async {
            val days = CourseTimeUtils.getDayInWeek(viewData.weekNum, viewData.startDate, viewData.weekStart, showWeekend)
            ScheduleHeaderView(context).apply {
                setSchedulePredefine(schedulePredefine)
                setScheduleViewData(viewData)
                setDays(days)
            }
        }

        val columnAmount = if (showWeekend) MAX_SCHEDULE_COLUMN_COUNT else MIN_SCHEDULE_COLUMN_COUNT
        val scheduleGridView = ScheduleGridView(context).apply {
            setSchedulePredefine(schedulePredefine)
            setScheduleViewData(viewData)
            setColumnAmount(columnAmount)
        }

        for (viewDeferred in cellsDeferred) {
            scheduleGridView.addScheduleCellPreventLayout(viewDeferred.await())
        }

        val scheduleView = ScheduleView(context, viewData.styles, columnAmount, scheduleHeaderViewDeferred.await(), scheduleGridView)
        if (listener != null) scheduleView.setOnCourseClickListener(listener)

        return@withContext if (viewData.styles.enableScheduleGridScroll || noScroll) {
            scheduleView
        } else {
            ScheduleScrollView(context).apply {
                addInnerView(scheduleView)
            }
        }
    }

    suspend fun generateScheduleImageByWeekNum(scheduleId: Long, weekNum: Int, @Px targetWidth: Int, waterMark: Boolean) = withContext(Dispatchers.Default) {
        val schedule = ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull() ?: return@withContext null
        val courses = ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).first()
        val styles = ScheduleDataStore.scheduleStylesFlow.firstOrNull()?.copy(
            viewAlpha = 100,
            timeTextColor = null,
            cornerScreenMargin = false,
            enableScheduleGridScroll = false
        ) ?: return@withContext null

        val backgroundColor = ApplicationInstance.getDefaultBackgroundColor()
        val viewData = CourseUtils.getScheduleViewDataByWeek(weekNum, ScheduleBuildBundle(schedule, courses, styles))
        val scheduleView = buildScheduleView(ApplicationInstance, viewData, noScroll = true)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.AT_MOST)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        scheduleView.measure(widthSpec, heightSpec)
        scheduleView.layout(0, 0, scheduleView.measuredWidth, scheduleView.measuredHeight)
        scheduleView.requestLayout()

        return@withContext Bitmap.createBitmap(scheduleView.measuredWidth, scheduleView.measuredHeight, Bitmap.Config.ARGB_8888).applyCanvas {
            drawColor(backgroundColor)
            scheduleView.draw(this)
        }.apply {
            if (waterMark) drawWaterMark(ApplicationInstance, this, ApplicationInstance.getString(R.string.app_name))
        }
    }

    private fun drawWaterMark(context: Context, bitmap: Bitmap, text: String) {
        val textPadding = context.resources.getDimensionPixelSize(R.dimen.water_mark_text_padding)
        val waterPrintSize = context.resources.getDimensionPixelSize(R.dimen.water_mark_text_size).toFloat()
        val paint = Paint().apply {
            color = context.getColorCompat(R.color.theme_color_secondary_text)
            alpha = context.resources.getInteger(R.integer.default_water_mark_alpha)
            textSize = waterPrintSize
            isAntiAlias = true
            isFakeBoldText = true
        }
        bitmap.applyCanvas {
            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            val baseLineHeight = paint.textBaselineHeight
            drawText(text, width - rect.width().toFloat() - textPadding, height - rect.height().toFloat() + baseLineHeight - textPadding, paint)
        }
    }

    suspend fun buildPreviewScheduleView(context: Context, previewStyles: SchedulePreviewStyles): View {
        val today = Date()

        val viewData = ScheduleViewData(
            scheduleId = 0,
            startDate = CalendarUtils.getFirstDateInThisWeek(today, previewStyles.weekStart),
            endDate = CalendarUtils.getLastDateInThisWeek(today, previewStyles.weekStart),
            weekNum = 1,
            weekStart = previewStyles.weekStart,
            times = SAMPLE_SCHEDULE_TIMES,
            cells = SAMPLE_COURSE_CELLS,
            styles = previewStyles.scheduleStyles
        )

        return buildScheduleView(context, viewData)
    }
}