package tool.xfy9326.schedule.ui.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.base.NetworkCourseProvider
import tool.xfy9326.schedule.content.beans.NetworkCourseImportParams
import tool.xfy9326.schedule.io.GlobalIO
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel

class NetworkCourseProviderViewModel : CourseProviderViewModel<NetworkCourseImportParams, NetworkCourseProvider<*>, NetworkCourseParser>() {
    val loginParams = MutableEventLiveData<NetworkLoginParams?>()
    val refreshCaptcha = MutableEventLiveData<ByteArray?>()

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
                    GlobalIO.resources.getStringArray(optionsRes)
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
                        it.loadCaptchaImage(importOption)
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
        courseParser: NetworkCourseParser,
    ): Pair<List<ScheduleTime>, List<Course>> {
        if (courseProvider is LoginCourseProvider) {
            courseProvider.login(importParams.userId!!, importParams.userPw!!, importParams.captchaCode, importOption)
        }

        val scheduleTimesHtml = courseProvider.loadScheduleTimesHtml(importOption)
        val coursesHtml = courseProvider.loadCoursesHtml(importOption)
        courseProvider.close()

        val scheduleTimes = courseParser.parseScheduleTimes(importOption, scheduleTimesHtml)
        val courses = courseParser.parseCourses(importOption, coursesHtml)

        return scheduleTimes to courses
    }

    class NetworkLoginParams(val options: Array<String>?, val enableCaptcha: Boolean, val allowLogin: Boolean)
}