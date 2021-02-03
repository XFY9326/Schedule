package tool.xfy9326.schedule.ui.vm

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.content.base.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.io.GlobalIO
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CourseUtils
import tool.xfy9326.schedule.utils.ScheduleUtils
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

class NetworkCourseProviderViewModel : AbstractViewModel() {
    lateinit var importConfig: CourseImportConfig<*, NetworkCourseProvider<*>, NetworkCourseParser>
        private set
    private lateinit var courseProvider: NetworkCourseProvider<*>
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

    fun registerConfig(config: CourseImportConfig<*, NetworkCourseProvider<*>, NetworkCourseParser>) {
        if (!::importConfig.isInitialized || importConfig != config) {
            importConfig = config
            courseProvider = config.newProvider()
            courseParser = config.newParser()
        }
    }

    fun isLoginCourseProvider() = courseProvider is LoginCourseProvider

    fun initLoginParams() {
        if (loginParamsLock.tryLock()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val provider = courseProvider
                    provider.init()

                    val optionsRes = importConfig.staticImportOptionsResId
                    val options = if (optionsRes == null) {
                        courseProvider.loadImportOptions()
                    } else {
                        GlobalIO.resources.getStringArray(optionsRes)
                    }

                    val captchaImage = if (provider is LoginCourseProvider) {
                        provider.getCaptchaImage()
                    } else {
                        null
                    }

                    loginParams.postEvent(LoginParams(options, captchaImage, provider is LoginCourseProvider))
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
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val provider = courseProvider

                    val captchaImage = if (provider is LoginCourseProvider) {
                        provider.getCaptchaImage(importOption)
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

    fun importCourse(importParams: NetworkImportParams, currentSchedule: Boolean, newScheduleName: String? = null) {
        if (isImportingCourses.compareAndSet(false, true)) {
            importCourseJob = viewModelScope.launch(Dispatchers.Default) {
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

                    if (courses.isEmpty() && !AppSettingsDataStore.allowImportEmptyScheduleFlow.first()) {
                        courseImportFinish.postEvent(false to false)
                        providerError.postEvent(CourseAdapterException.ErrorType.SCHEDULE_COURSE_IMPORT_EMPTY.make())
                        return@launch
                    }

                    val scheduleTimeValid = ScheduleUtils.validateScheduleTime(scheduleTimes)
                    if (!scheduleTimeValid) {
                        courseImportFinish.postEvent(false to false)
                        providerError.postEvent(CourseAdapterException.ErrorType.SCHEDULE_TIMES_ERROR.make())
                        return@launch
                    }

                    val hasConflicts = CourseUtils.solveConflicts(scheduleTimes, courses)

                    if (currentSchedule) {
                        ScheduleUtils.saveCurrentSchedule(scheduleTimes, courses)
                    } else {
                        ScheduleUtils.saveNewSchedule(newScheduleName, scheduleTimes, courses)
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

    class LoginParams(val options: Array<String>?, val captcha: Bitmap?, val allowLogin: Boolean)

    class NetworkImportParams(val userId: String?, val userPw: String?, val captchaCode: String?, val importOption: Int)
}