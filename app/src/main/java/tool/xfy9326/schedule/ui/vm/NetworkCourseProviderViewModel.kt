package tool.xfy9326.schedule.ui.vm

import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.NetworkCourseImportParams
import tool.xfy9326.schedule.beans.NetworkLoginParams
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.base.NetworkCourseProvider
import tool.xfy9326.schedule.io.IOManager
import tool.xfy9326.schedule.tools.livedata.MutableEventLiveData
import tool.xfy9326.schedule.tools.livedata.postEvent
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel

class NetworkCourseProviderViewModel : CourseProviderViewModel<NetworkCourseImportParams, NetworkCourseProvider<*>, NetworkCourseParser<*>>() {
    val loginParams = MutableEventLiveData<NetworkLoginParams?>()
    val refreshCaptcha = MutableEventLiveData<BitmapDrawable?>()

    private val captchaLock = Mutex()
    private val loginParamsLock = Mutex()

    val isLoginCourseProvider
        get() = courseProvider is LoginCourseProvider

    fun initLoginParams() {
        providerFunctionRunner(loginParamsLock, Dispatchers.IO,
            onRun = {
                it.init()

                val optionsRes = importConfig.staticImportOptionsResId
                val options = if (optionsRes == null) {
                    courseProvider.loadImportOptions()
                } else {
                    IOManager.resources.getStringArray(optionsRes)
                }
                val enableCaptcha = it !is LoginCourseProvider || it.enableCaptcha

                loginParams.postEvent(NetworkLoginParams(options, enableCaptcha, it is LoginCourseProvider))
            },
            onFailed = {
                loginParams.postEvent(null)
            }
        )
    }

    fun refreshCaptcha(importOption: Int) {
        providerFunctionRunner(captchaLock, Dispatchers.IO,
            onRun = {
                refreshCaptcha.postEvent(
                    if (it is LoginCourseProvider && it.enableCaptcha) {
                        it.loadCaptchaImage(importOption)?.toDrawable(IOManager.resources)
                    } else {
                        null
                    }
                )
            },
            onFailed = {
                refreshCaptcha.postEvent(null)
            }
        )
    }

    override suspend fun onImportCourse(
        importParams: NetworkCourseImportParams,
        importOption: Int,
        courseProvider: NetworkCourseProvider<*>,
        courseParser: NetworkCourseParser<*>,
    ): ImportContent {
        if (courseProvider is LoginCourseProvider) {
            courseProvider.login(importParams.userId!!, importParams.userPw!!, importParams.captchaCode, importOption)
        }

        val scheduleTimesHtml: String?
        val coursesHtml: String
        try {
            scheduleTimesHtml = courseProvider.loadScheduleTimesHtml(importOption)
            coursesHtml = courseProvider.loadCoursesHtml(importOption)
        } finally {
            courseProvider.close()
        }

        val scheduleTimes = courseParser.parseScheduleTimes(importOption, scheduleTimesHtml)
        val coursesParseResult = courseParser.parseCourses(importOption, coursesHtml)

        return ImportContent(scheduleTimes, coursesParseResult)
    }

}