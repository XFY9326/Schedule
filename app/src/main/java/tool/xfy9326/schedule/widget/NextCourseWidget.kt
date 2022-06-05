package tool.xfy9326.schedule.widget

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import io.github.xfy9326.atools.coroutines.goAsync
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.utils.schedule.NextCourseUtils
import tool.xfy9326.schedule.utils.view.NextCourseWidgetUtils

/**
 * 只被用为BroadcastReceiver与具体Size的父类
 * 实现不同Size的AppWidget的逻辑代码复用
 */
open class NextCourseWidget : AppWidgetProvider() {
    companion object {
        const val ACTION_WIDGET_NEXT_COURSE_REFRESH = "${BuildConfig.APPLICATION_ID}.action.WIDGET_NEXT_COURSE_REFRESH"
        const val EXTRA_NEXT_COURSE = "EXTRA_NEXT_COURSE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action ?: return

        if (action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            goAsync {
                if (NextCourseWidgetUtils.hasNextCourseWidget(context)) {
                    val nextCourse = NextCourseUtils.getCurrentScheduleNextCourse()
                    NextCourseWidgetUtils.setupNextAlarm(context, nextCourse)
                }
            }
        } else if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || action == AppWidgetManager.ACTION_APPWIDGET_RESTORED) { // 来自系统的更新（只刷新指定的Widget）
            goAsync {
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: return@goAsync
                val nextCourse = NextCourseUtils.getCurrentScheduleNextCourse()
                NextCourseWidgetUtils.setupNextAlarm(context, nextCourse)
                val remoteViews = NextCourseWidgetUtils.generateRemoteViews(context, nextCourse, this::class)
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, remoteViews)
            }
        } else if (action == ACTION_WIDGET_NEXT_COURSE_REFRESH) { // 来自App内部的更新（全局刷新）
            goAsync {
                val appWidgetIds = NextCourseWidgetUtils.getAllNextCourseWidgetId(context)
                if (NextCourseWidgetUtils.hasNextCourseWidget(context, appWidgetIds)) {
                    val nextCourse = intent.getParcelableExtra(EXTRA_NEXT_COURSE) ?: NextCourseUtils.getCurrentScheduleNextCourse()
                    NextCourseWidgetUtils.setupNextAlarm(context, nextCourse)
                    for ((clazz, idArray) in appWidgetIds) {
                        val remoteViews = NextCourseWidgetUtils.generateRemoteViews(context, nextCourse, clazz)
                        AppWidgetManager.getInstance(context).updateAppWidget(idArray, remoteViews)
                    }
                } else {
                    NextCourseWidgetUtils.cancelAllAlarm(context)
                }
            }
        } else if (action == AppWidgetManager.ACTION_APPWIDGET_DISABLED) {
            if (!NextCourseWidgetUtils.hasNextCourseWidget(context)) {
                NextCourseWidgetUtils.cancelAllAlarm(context)
            }
        }
    }

    class Size4x1 : NextCourseWidget()

    class Size2x2 : NextCourseWidget()

    class Size2x1 : NextCourseWidget()
}