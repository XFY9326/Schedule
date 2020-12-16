package tool.xfy9326.schedule.ui.vm

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.base.NetworkCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CourseManager
import tool.xfy9326.schedule.utils.ScheduleManager
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

class NetworkCourseProviderViewModel : AbstractViewModel() {
    lateinit var importConfig: CourseImportConfig<NetworkCourseProvider, NetworkCourseParser>
        private set
    private lateinit var courseProvider: NetworkCourseProvider
    private lateinit var courseParser: NetworkCourseParser

    val loginParams = MutableEventLiveData<LoginParams?>()
    val providerError = MutableEventLiveData<CourseAdapterException>()
    val refreshCaptcha = MutableEventLiveData<Bitmap?>()

    // Import success / Has conflicts
    val courseImportFinish = MutableEventLiveData<Pair<Boolean, Boolean>>()

    private val captchaLock = Mutex()
    private val loginParamsLock = Mutex()
    private var importCourseJob: Job? = null

    val isImportingCourses = AtomicBoolean(false)

    fun registerConfig(config: CourseImportConfig<NetworkCourseProvider, NetworkCourseParser>) {
        if (!::importConfig.isInitialized || importConfig != config) {
            importConfig = config
            courseProvider = config.newProvider()
            courseParser = config.newParser()
        }
    }

    fun isLoginCourseProvider() = courseProvider is LoginCourseProvider

    fun initLoginParams() {
        if (loginParamsLock.tryLock()) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val provider = courseProvider
                    provider.init()

                    val optionsRes = importConfig.importOptions
                    val optionsOnline =
                        if (optionsRes == null) {
                            courseProvider.loadImportOptions()
                        } else {
                            null
                        }

                    val captchaImage = if (provider is LoginCourseProvider) {
                        val captchaUrl = provider.loadCaptchaUrl(0)
                        if (captchaUrl != null) {
                            provider.getCaptchaImage(captchaUrl)
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                    loginParams.postEvent(LoginParams(optionsOnline, optionsRes, captchaImage, provider is LoginCourseProvider))
                } catch (e: CourseAdapterException) {
                    loginParams.postEvent(null)
                    providerError.postEvent(e)
                } catch (e: Exception) {
                    loginParams.postEvent(null)
                    providerError.postEvent(CourseAdapterException.ErrorType.UNKNOWN_ERROR.make(e))
                } finally {
                    loginParamsLock.unlock()
                }
            }
        }
    }

    fun refreshCaptcha(importOption: Int) {
        if (captchaLock.tryLock()) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val provider = courseProvider

                    val captchaImage = if (provider is LoginCourseProvider) {
                        val captchaUrl = provider.loadCaptchaUrl(importOption)
                        if (captchaUrl != null) {
                            provider.getCaptchaImage(captchaUrl)
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                    refreshCaptcha.postEvent(captchaImage)
                } catch (e: CourseAdapterException) {
                    refreshCaptcha.postEvent(null)
                    providerError.postEvent(e)
                } catch (e: Exception) {
                    refreshCaptcha.postEvent(null)
                    providerError.postEvent(CourseAdapterException.ErrorType.UNKNOWN_ERROR.make(e))
                } finally {
                    captchaLock.unlock()
                }
            }
        }
    }

    fun importCourse(importParams: ImportParams, currentSchedule: Boolean, newScheduleName: String? = null) {
        if (isImportingCourses.compareAndSet(false, true)) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val provider = courseProvider

                    if (provider is LoginCourseProvider) {
                        provider.login(importParams.userId!!, importParams.userPw!!, importParams.captchaCode, importParams.importOption)
                    }

                    val scheduleTimesHtml = provider.loadScheduleTimesHtml(importParams.importOption)
                    val coursesHtml = provider.loadCoursesHtml(importParams.importOption)
                    provider.close()

                    val scheduleTimes = courseParser.parseScheduleTimes(importParams.importOption, scheduleTimesHtml)
                    val courses = courseParser.parseCourses(importParams.importOption, coursesHtml)

                    val hasConflicts = CourseManager.solveConflicts(scheduleTimes, courses)

                    if (currentSchedule) {
                        ScheduleManager.saveCurrentSchedule(scheduleTimes, courses)
                    } else {
                        ScheduleManager.saveNewSchedule(newScheduleName, scheduleTimes, courses)
                    }

                    courseImportFinish.postEvent(true to hasConflicts)
                } catch (e: CourseAdapterException) {
                    courseImportFinish.postEvent(false to false)
                    providerError.postEvent(e)
                } catch (e: Exception) {
                    courseImportFinish.postEvent(false to false)
                    if (e is SocketTimeoutException || e is SocketException || e is ConnectException || e is UnknownHostException) {
                        providerError.postEvent(CourseAdapterException.ErrorType.CONNECTION_ERROR.make(e))
                    } else if (e !is CancellationException) {
                        providerError.postEvent(CourseAdapterException.ErrorType.UNKNOWN_ERROR.make(e))
                    }
                } finally {
                    isImportingCourses.set(false)
                    importCourseJob = null
                }
            }
        }
    }

    fun finishImport() {
        importCourseJob?.cancel()
        isImportingCourses.set(false)
    }

    override fun onCleared() {
        finishImport()
    }

    class LoginParams(val optionsOnline: Array<String>?, val optionsRes: Int?, val captcha: Bitmap?, val allowLogin: Boolean)

    class ImportParams(val userId: String?, val userPw: String?, val captchaCode: String?, val importOption: Int)
}