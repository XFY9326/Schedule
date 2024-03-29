package tool.xfy9326.schedule.utils.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.core.graphics.applyCanvas
import io.github.xfy9326.atools.core.AppContext
import io.github.xfy9326.atools.io.IOManager
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.textBaselineHeight
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.SchedulePreviewStyles
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleViewData
import tool.xfy9326.schedule.beans.SectionTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.view.schedule.IScheduleCell
import tool.xfy9326.schedule.ui.view.schedule.ScheduleCourseCellView
import tool.xfy9326.schedule.ui.view.schedule.ScheduleGridView
import tool.xfy9326.schedule.ui.view.schedule.ScheduleHeaderView
import tool.xfy9326.schedule.ui.view.schedule.ScheduleScrollView
import tool.xfy9326.schedule.ui.view.schedule.ScheduleTimeCellView
import tool.xfy9326.schedule.ui.view.schedule.ScheduleView
import tool.xfy9326.schedule.utils.CalendarUtils
import tool.xfy9326.schedule.utils.getDefaultBackgroundColor
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import tool.xfy9326.schedule.utils.schedule.CourseUtils
import java.util.Date

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
        val sampleTeacher = IOManager.resources.getString(R.string.sample_course_teacher)
        listOf(
            CourseCell(0, 0, sampleName, sampleLocation, sampleTeacher, SectionTime(WeekDay.MONDAY, 1, 2), colors[0], 1, true),
            CourseCell(0, 1, sampleName, null, null, SectionTime(WeekDay.MONDAY, 3, 2), colors[2], 1, false),
            CourseCell(0, 2, sampleName, sampleLocation, sampleTeacher, SectionTime(WeekDay.TUESDAY, 4, 4), colors[4], 1, true),
            CourseCell(0, 3, sampleName, sampleLocation, null, SectionTime(WeekDay.WEDNESDAY, 2, 2), colors[6], 1, true),
            CourseCell(1, 4, sampleName, null, sampleTeacher, SectionTime(WeekDay.WEDNESDAY, 5, 2), colors[8], 1, false),
            CourseCell(1, 5, sampleName, null, sampleTeacher, SectionTime(WeekDay.FRIDAY, 4, 3), colors[10], 1, false),
            CourseCell(1, 6, sampleName, sampleLocation, null, SectionTime(WeekDay.FRIDAY, 8, 2), colors[12], 1, true),
            CourseCell(2, 7, sampleName, null, null, SectionTime(WeekDay.THURSDAY, 8, 2), colors[1], 1, true),
            CourseCell(2, 8, sampleName, sampleLocation, sampleTeacher, SectionTime(WeekDay.THURSDAY, 6, 2), colors[3], 1, false),
            CourseCell(2, 9, sampleName, sampleLocation, null, SectionTime(WeekDay.TUESDAY, 9, 2), colors[5], 1, true),
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

        val cellsDeferred = ArrayList<Deferred<IScheduleCell>>(viewData.times.size + viewData.cells.size)

        for ((i, time) in viewData.times.withIndex()) {
            cellsDeferred.add(async { ScheduleTimeCellView(context, i, time, schedulePredefine, viewData.styles) })
        }

        for (cell in viewData.cells) {
            if (cell.isThisWeekCourse || viewData.styles.showNotThisWeekCourse) {
                cellsDeferred.add(async {
                    ScheduleCourseCellView(
                        context,
                        showWeekend,
                        cell,
                        schedulePredefine,
                        viewData.styles,
                        viewData.weekStart
                    )
                })
            }
        }

        val scheduleHeaderViewDeferred = async {
            val days = CourseTimeUtils.getDayInWeek(viewData.weekNum, viewData.startDate, viewData.weekStart, showWeekend)
            ScheduleHeaderView(context, days, schedulePredefine, viewData.styles)
        }

        val columnAmount = if (showWeekend) MAX_SCHEDULE_COLUMN_COUNT else MIN_SCHEDULE_COLUMN_COUNT
        val scheduleGridView = ScheduleGridView(context, columnAmount, viewData.rowAmount, schedulePredefine, viewData.styles)

        for (viewDeferred in cellsDeferred) {
            scheduleGridView.addScheduleCell(viewDeferred.await())
        }

        val scheduleView = ScheduleView(context, viewData.styles, columnAmount, scheduleHeaderViewDeferred.await(), scheduleGridView, noScroll)
        if (listener != null) scheduleView.setOnCourseClickListener(listener)

        return@withContext if (viewData.styles.enableScheduleGridScroll || noScroll) {
            scheduleView
        } else {
            ScheduleScrollView(context).apply {
                addInnerView(scheduleView)
            }
        }
    }

    suspend fun generateScheduleImageByWeekNum(scheduleId: Long, weekNum: Int, @Px targetWidth: Int, @Px suggestHeight: Int, waterMark: Boolean) =
        withContext(Dispatchers.Default) {
            val schedule = ScheduleDBProvider.db.scheduleDAO.getSchedule(scheduleId).firstOrNull() ?: return@withContext null
            val courses = ScheduleDBProvider.db.scheduleDAO.getScheduleCourses(scheduleId).first()
            val styles = ScheduleDataStore.scheduleStylesFlow.firstOrNull()?.copy(
                viewAlpha = 100,
                timeTextColor = null,
                cornerScreenMargin = false,
                enableScheduleGridScroll = false
            ) ?: return@withContext null

            val backgroundColor = AppContext.getDefaultBackgroundColor()
            val viewData = CourseUtils.getScheduleViewDataByWeek(weekNum, ScheduleBuildBundle(schedule, courses, styles))
            val scheduleView = buildScheduleView(AppContext, viewData, noScroll = true)

            val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.AT_MOST)
            // styles.courseCellFullScreenSameHeight 需要具体高度计算每一个Cell大小，要分开判断
            val heightSpec = if (suggestHeight == 0 && !styles.courseCellFullScreenSameHeight) {
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            } else {
                View.MeasureSpec.makeMeasureSpec(suggestHeight, View.MeasureSpec.AT_MOST)
            }

            scheduleView.measure(widthSpec, heightSpec)
            scheduleView.layout(0, 0, scheduleView.measuredWidth, scheduleView.measuredHeight)
            scheduleView.requestLayout()

            return@withContext Bitmap.createBitmap(scheduleView.measuredWidth, scheduleView.measuredHeight, Bitmap.Config.ARGB_8888).applyCanvas {
                drawColor(backgroundColor)
                scheduleView.draw(this)
            }.apply {
                if (waterMark) drawWaterMark(AppContext, this, AppContext.getString(R.string.app_name))
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