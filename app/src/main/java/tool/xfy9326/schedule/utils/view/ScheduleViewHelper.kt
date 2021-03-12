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
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseCell
import tool.xfy9326.schedule.beans.ScheduleBuildBundle
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.kt.getColorCompat
import tool.xfy9326.schedule.kt.getDefaultBackgroundColor
import tool.xfy9326.schedule.ui.view.*
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import tool.xfy9326.schedule.utils.schedule.CourseUtils

object ScheduleViewHelper {
    private const val MIN_SCHEDULE_COLUMN_COUNT = 6
    private const val MAX_SCHEDULE_COLUMN_COUNT = 8

    suspend fun buildScheduleView(
        context: Context,
        weekNum: Int,
        scheduleBuildBundle: ScheduleBuildBundle,
        listener: ((CourseCell) -> Unit)? = null,
        noScroll: Boolean = false,
    ): View = withContext(Dispatchers.Default) {
        val viewData = CourseUtils.getScheduleViewDataByWeek(weekNum, scheduleBuildBundle)

        val schedulePredefine = SchedulePredefine.load(context)
        val showWeekend = viewData.styles.forceShowWeekendColumn || viewData.hasWeekendCourse

        val cellsDeferred = ArrayList<Deferred<ScheduleCellView>>(viewData.times.size + viewData.cells.size)

        for ((i, time) in viewData.times.withIndex()) {
            cellsDeferred.add(async { ScheduleCellView(context, i, time, schedulePredefine, viewData.styles) })
        }

        for (cell in viewData.cells) {
            cellsDeferred.add(async { ScheduleCellView(context, showWeekend, cell, schedulePredefine, viewData.styles, viewData.weekStart) })
        }

        val days = CourseTimeUtils.getDayInWeek(viewData.weekNum, viewData.startDate, viewData.weekStart, showWeekend)

        val scheduleHeaderViewDeferred = async { ScheduleHeaderView(context, viewData, days, schedulePredefine) }

        val columnAmount = if (showWeekend) MAX_SCHEDULE_COLUMN_COUNT else MIN_SCHEDULE_COLUMN_COUNT
        val scheduleGridView = ScheduleGridView(context, viewData, columnAmount, schedulePredefine)

        for (viewDeferred in cellsDeferred) {
            viewDeferred.await().let(scheduleGridView::addScheduleCellWithoutLayout)
        }

        val scheduleView = ScheduleView(context, viewData.styles, columnAmount, scheduleHeaderViewDeferred.await(), scheduleGridView)
        if (listener != null) scheduleView.setOnCourseClickListener(listener)

        return@withContext if (viewData.styles.enableScheduleGridScroll || noScroll) scheduleView else ScheduleScrollLayout(context, scheduleView)
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

        val context = App.instance

        val backgroundColor = context.getDefaultBackgroundColor()
        val scheduleView = buildScheduleView(context, weekNum, ScheduleBuildBundle(schedule, courses, styles), noScroll = true)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.AT_MOST)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        scheduleView.measure(widthSpec, heightSpec)
        scheduleView.layout(0, 0, scheduleView.measuredWidth, scheduleView.measuredHeight)
        scheduleView.requestLayout()

        return@withContext Bitmap.createBitmap(scheduleView.measuredWidth, scheduleView.measuredHeight, Bitmap.Config.ARGB_8888).applyCanvas {
            drawColor(backgroundColor)
            scheduleView.draw(this)
        }.apply {
            if (waterMark) drawWaterMark(context, this, context.getString(R.string.app_name))
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
            val font = paint.fontMetrics
            val baseLineHeight = (font.descent - font.ascent) / 2
            drawText(text, width - rect.width().toFloat() - textPadding, height - rect.height().toFloat() + baseLineHeight - textPadding, paint)
        }
    }
}