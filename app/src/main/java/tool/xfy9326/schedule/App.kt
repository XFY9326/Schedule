@file:Suppress("unused")

package tool.xfy9326.schedule

import android.app.Application
import lib.xfy9326.android.kit.initializeToolKit
import tool.xfy9326.schedule.tools.ExceptionHandler
import tool.xfy9326.schedule.utils.view.NextCourseWidgetUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 工具类初始化
        initializeToolKit()

        // 异常收集器初始化
        ExceptionHandler.init()

        // 初始化夜间模式设定
        ViewUtils.initNightMode()

        // 初始化'下一节课'的数据监听
        NextCourseWidgetUtils.initDataObserver()
    }
}