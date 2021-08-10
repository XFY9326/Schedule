package tool.xfy9326.schedule.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import lib.xfy9326.android.kit.goAsync
import lib.xfy9326.android.kit.showGlobalToast
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
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

        private fun handleReceiver(widget: NextCourseWidget, context: Context?, intent: Intent?) {
            if (context == null) return
            val action = intent?.action ?: return

            if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || action == AppWidgetManager.ACTION_APPWIDGET_RESTORED) { // 来自系统的更新（只刷新指定的Widget）
                widget.goAsync {
                    val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: return@goAsync
                    val nextCourse = NextCourseUtils.getCurrentScheduleNextCourse()
                    NextCourseWidgetUtils.setupNextAlarm(context, nextCourse)
                    val remoteViews = NextCourseWidgetUtils.generateRemoteViews(context, nextCourse, widget::class)
                    AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, remoteViews)
                }
            } else if (action == ACTION_WIDGET_NEXT_COURSE_REFRESH) { // 来自App内部的更新（全局刷新）
                widget.goAsync {
                    val appWidgetIds = NextCourseWidgetUtils.getAllNextCourseWidgetId(context)
                    val nextCourse = intent.getParcelableExtra(EXTRA_NEXT_COURSE) ?: NextCourseUtils.getCurrentScheduleNextCourse()
                    NextCourseWidgetUtils.setupNextAlarm(context, nextCourse)
                    for ((clazz, idArray) in appWidgetIds) {
                        val remoteViews = NextCourseWidgetUtils.generateRemoteViews(context, nextCourse, clazz)
                        AppWidgetManager.getInstance(context).updateAppWidget(idArray, remoteViews)
                    }
                }
            } else if (action == AppWidgetManager.ACTION_APPWIDGET_ENABLED) {
                // TODO: Change it in SDK 31 https://developer.android.google.cn/about/versions/12/behavior-changes-12#exact-alarm-permission
                widget.goAsync {
                    if (AppDataStore.showAppWidgetAttentionFlow.first()) {
                        AppDataStore.setShowAppWidgetAttention(false)
                        withContext(Dispatchers.Main) {
                            showGlobalToast(R.string.app_widget_attention, showLong = true)
                        }
                    }
                }
            } else if (action == AppWidgetManager.ACTION_APPWIDGET_DISABLED) {
                NextCourseWidgetUtils.cancelAllAlarmIfNoWidget(context)
                widget.goAsync {
                    if (!NextCourseWidgetUtils.hasNextCourseWidget(context)) {
                        AppDataStore.setShowAppWidgetAttention(true)
                    }
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) = handleReceiver(this, context, intent)

    class Size4x1 : NextCourseWidget()

    class Size2x2 : NextCourseWidget()

    class Size2x1 : NextCourseWidget()
}