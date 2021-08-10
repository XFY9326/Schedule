package tool.xfy9326.schedule.utils.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lib.xfy9326.android.kit.ApplicationInstance
import lib.xfy9326.android.kit.ApplicationScope
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.NextCourse
import tool.xfy9326.schedule.beans.NextCourseInfo
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.schedule.NextCourseUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleDataProcessor
import tool.xfy9326.schedule.widget.NextCourseWidget
import kotlin.reflect.KClass

object NextCourseWidgetUtils {
    private const val REQUEST_CODE_WIDGET_NEXT_COURSE_REFRESH = 1
    private const val REQUEST_CODE_WIDGET_NEXT_COURSE_LAUNCH_APP = 2

    private val widgetClasses = arrayOf(NextCourseWidget.Size4x1::class, NextCourseWidget.Size2x2::class, NextCourseWidget.Size2x1::class)
    private val observerJobLock = Mutex()
    private var observerJob: Job? = null

    fun initDataObserver() {
        if (hasNextCourseWidget(ApplicationInstance)) {
            ApplicationScope.launch {
                observerJobLock.withLock {
                    if (observerJob?.isActive != true) {
                        observerJob = ScheduleDataProcessor.addCurrentScheduleCourseDataGlobalListener { schedule ->
                            if (hasNextCourseWidget(ApplicationInstance)) {
                                val nextCourse = NextCourseUtils.getNextCourseByDate(schedule)
                                updateAllNextCourseWidget(ApplicationInstance, nextCourse)
                            } else {
                                stopDataObserver()
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun stopDataObserver() {
        observerJobLock.withLock {
            if (observerJob?.isActive != true) {
                observerJob?.cancel()
                observerJob = null
            }
        }
    }

    private fun updateAllNextCourseWidget(context: Context, nextCourse: NextCourse) =
        context.sendBroadcast(Intent(context, NextCourseWidget::class.java).apply {
            action = NextCourseWidget.ACTION_WIDGET_NEXT_COURSE_REFRESH
            putExtra(NextCourseWidget.EXTRA_NEXT_COURSE, nextCourse)
        })

    fun hasNextCourseWidget(context: Context): Boolean {
        val idMap = getAllNextCourseWidgetId(context)
        for (entry in idMap) {
            if (entry.value.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    private fun getWidgetRefreshPendingIntent(context: Context) =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_WIDGET_NEXT_COURSE_REFRESH,
            Intent(context, NextCourseWidget::class.java).apply { action = NextCourseWidget.ACTION_WIDGET_NEXT_COURSE_REFRESH },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun getAllNextCourseWidgetId(context: Context): HashMap<KClass<out NextCourseWidget>, IntArray> {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val resultMap = HashMap<KClass<out NextCourseWidget>, IntArray>()
        for (widgetClass in widgetClasses) {
            resultMap[widgetClass] = appWidgetManager.getAppWidgetIds(ComponentName(context, widgetClass.java))
        }
        return resultMap
    }

    fun setupNextAlarm(context: Context, nextCourse: NextCourse) {
        val pendingIntent = getWidgetRefreshPendingIntent(context)
        context.getSystemService<AlarmManager>()?.apply {
            if (nextCourse.nextAutoRefreshTime > 0) {
                AlarmManagerCompat.setExactAndAllowWhileIdle(this, AlarmManager.RTC, nextCourse.nextAutoRefreshTime, pendingIntent)
            } else {
                cancel(pendingIntent)
            }
        }
    }

    fun cancelAllAlarmIfNoWidget(context: Context) {
        if (!hasNextCourseWidget(context)) {
            context.getSystemService<AlarmManager>()?.cancel(getWidgetRefreshPendingIntent(context))
        }
    }

    fun generateRemoteViews(context: Context, nextCourse: NextCourse, clazz: KClass<out NextCourseWidget>) =
        when {
            nextCourse.isVacation ->
                buildWidgetMessageView(context, getLayoutId(clazz, true), R.drawable.ic_surfing_24, R.string.next_course_widget_in_vacation)
            nextCourse.noNextCourse ->
                buildWidgetMessageView(context, getLayoutId(clazz, true), R.drawable.ic_break_24, R.string.next_course_widget_no_next_course)
            nextCourse.nextCourseInfo != null ->
                buildWidgetNextCourseView(context, getLayoutId(clazz, false), nextCourse.nextCourseInfo)
            else ->
                buildWidgetMessageView(context, getLayoutId(clazz, true), R.drawable.ic_data_24, R.string.next_course_widget_no_data)
        }

    @LayoutRes
    private fun getLayoutId(clazz: KClass<out NextCourseWidget>, isMsg: Boolean) =
        when (clazz) {
            NextCourseWidget.Size4x1::class ->
                if (isMsg) R.layout.widget_next_course_4x1_msg else R.layout.widget_next_course_4x1
            NextCourseWidget.Size2x2::class ->
                if (isMsg) R.layout.widget_next_course_2x2_msg else R.layout.widget_next_course_2x2
            NextCourseWidget.Size2x1::class ->
                if (isMsg) R.layout.widget_next_course_2x1_msg else R.layout.widget_next_course_2x1
            else -> error("Unsupported NextCourseWidget size class! Class: $clazz")
        }

    private fun getContentClickPendingIntent(context: Context) =
        PendingIntent.getActivity(context,
            REQUEST_CODE_WIDGET_NEXT_COURSE_LAUNCH_APP,
            IntentUtils.getLaunchAppIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun buildWidgetMessageView(context: Context, @LayoutRes layoutRes: Int, @DrawableRes iconRes: Int, @StringRes msgRes: Int) =
        RemoteViews(context.packageName, layoutRes).apply {
            setTextViewText(R.id.textView_nextCourseWidgetMsg, context.getString(msgRes))
            setViewVisibility(R.id.imageView_nextCourseWidgetIcon, View.VISIBLE)
            setImageViewResource(R.id.imageView_nextCourseWidgetIcon, iconRes)

            setOnClickPendingIntent(R.id.layout_nextCourseWidgetContent, getContentClickPendingIntent(context))
        }

    private fun buildWidgetNextCourseView(context: Context, @LayoutRes layoutRes: Int, nextCourseInfo: NextCourseInfo) =
        RemoteViews(context.packageName, layoutRes).apply {
            val drawable = getCourseColorDrawable(context, nextCourseInfo.courseColor)
            setImageViewBitmap(R.id.imageView_nextCourseWidgetColor, drawable.toBitmap())

            setTextViewText(R.id.textView_nextCourseWidgetName, nextCourseInfo.courseName)

            if (layoutRes == R.layout.widget_next_course_2x2) {
                if (nextCourseInfo.courseTeacher == null) {
                    setViewVisibility(R.id.textView_nextCourseWidgetTeacher, View.GONE)
                } else {
                    setViewVisibility(R.id.textView_nextCourseWidgetTeacher, View.VISIBLE)
                    setTextViewText(R.id.textView_nextCourseWidgetTeacher, nextCourseInfo.courseTeacher)
                }
                if (nextCourseInfo.courseLocation == null) {
                    setViewVisibility(R.id.textView_nextCourseWidgetLocation, View.GONE)
                } else {
                    setViewVisibility(R.id.textView_nextCourseWidgetLocation, View.VISIBLE)
                    setTextViewText(R.id.textView_nextCourseWidgetLocation, nextCourseInfo.courseLocation)
                }
            } else {
                val description = nextCourseInfo.getSingleLineCourseTimeDescription(context)
                if (description == null) {
                    setViewVisibility(R.id.textView_nextCourseWidgetDescription, View.GONE)
                } else {
                    setViewVisibility(R.id.textView_nextCourseWidgetDescription, View.VISIBLE)
                    setTextViewText(R.id.textView_nextCourseWidgetDescription, description)
                }
            }

            setTextViewText(R.id.textView_nextCourseWidgetTime, nextCourseInfo.startTime)

            setOnClickPendingIntent(R.id.layout_nextCourseWidgetContent, getContentClickPendingIntent(context))
        }

    private fun getCourseColorDrawable(context: Context, @ColorInt color: Int) =
        ContextCompat.getDrawable(context, R.drawable.shape_circle_20)!!.also {
            it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
}