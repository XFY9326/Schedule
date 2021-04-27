package tool.xfy9326.schedule.ui.vm

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.beans.NetworkCourseImportParams
import tool.xfy9326.schedule.beans.NetworkProviderParams
import tool.xfy9326.schedule.beans.ScheduleImportContent
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.base.NetworkCourseProvider
import tool.xfy9326.schedule.content.beans.LoginPageInfo
import tool.xfy9326.schedule.io.kt.tryRecycle
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel

class NetworkCourseProviderViewModel : CourseProviderViewModel<NetworkCourseImportParams, NetworkCourseProvider<*>, NetworkCourseParser<*>>() {
    val loginCaptcha = MutableLiveData<Bitmap?>()
    val providerParams = MutableLiveData<NetworkProviderParams?>()
    val importOptions = MutableLiveData<Array<String>?>()

    private val loginPageInfoLock = Mutex()

    val isLoginCourseProvider
        get() = courseProvider is LoginCourseProvider

    private var loginPageInfo = LoginPageInfo.Empty

    override fun onProviderCreate() {
        providerFunctionRunner(
            onRun = {
                it.init()
                refreshProviderParams(provider = it)
                loadImportOptions()
            }
        )
    }

    private fun loadImportOptions() {
        providerFunctionRunner(
            onRun = {
                val options = importConfigInstance.staticImportOptions ?: it.loadImportOptions()
                importOptions.postValue(options)
            },
            onFailed = {
                importOptions.postValue(null)
            }
        )
    }

    fun refreshLoginPageInfo(importOption: Int): Boolean =
        providerFunctionRunner(loginPageInfoLock,
            onRun = {
                refreshProviderParams(importOption, it)
            },
            onFailed = {
                providerParams.postValue(null)
            }
        )

    private suspend fun refreshProviderParams(importOption: Int = 0, provider: NetworkCourseProvider<*>) {
        loadLoginPage(importOption, provider)

        val enableCaptcha = provider is LoginCourseProvider && provider.enableCaptcha

        if (enableCaptcha) {
            getCaptchaImage(importOption, provider)
        }

        providerParams.postValue(NetworkProviderParams(enableCaptcha, isLoginCourseProvider))
    }

    private suspend fun loadLoginPage(importOption: Int = 0, provider: NetworkCourseProvider<*>) {
        loginPageInfo = if (provider is LoginCourseProvider) {
            provider.loadLoginPage(importOption)
        } else {
            LoginPageInfo.Empty
        }
    }

    fun refreshCaptcha(importOption: Int) {
        providerFunctionRunner(loginPageInfoLock,
            onRun = {
                getCaptchaImage(importOption, it)
            },
            onFailed = {
                loginCaptcha.postValue(null)
            }
        )
    }

    private suspend fun getCaptchaImage(importOption: Int = 0, provider: NetworkCourseProvider<*>) {
        val url = loginPageInfo.captchaUrl
        if (provider is LoginCourseProvider && url != null) {
            loginCaptcha.postValue(provider.getCaptchaImage(url, importOption))
        } else {
            loginCaptcha.postValue(null)
        }
    }

    override suspend fun onImportCourse(
        importParams: NetworkCourseImportParams,
        importOption: Int,
        courseProvider: NetworkCourseProvider<*>,
        courseParser: NetworkCourseParser<*>,
    ): ScheduleImportContent {
        if (courseProvider is LoginCourseProvider) {
            courseProvider.login(importParams.userId, importParams.userPw, importParams.captchaCode, importParams.loginPageInfo, importOption)
        }

        val scheduleTimesHtml = courseProvider.loadScheduleTimesHtml(importOption)
        val coursesHtml = courseProvider.loadCoursesHtml(importOption)
        val termHtml = courseProvider.loadTermHtml(importOption)

        val scheduleTimes = courseParser.parseScheduleTimes(importOption, scheduleTimesHtml)
        val coursesParseResult = courseParser.parseCourses(importOption, coursesHtml)
        val term = courseParser.parseTerm(importOption, termHtml)

        return ScheduleImportContent(scheduleTimes, coursesParseResult, term)
    }

    override fun onProviderDestroy() {
        providerFunctionRunner(
            onRun = {
                courseProvider.close()

                val captcha = loginCaptcha.value
                loginCaptcha.postValue(null)
                captcha?.tryRecycle()
            }
        )
    }
}