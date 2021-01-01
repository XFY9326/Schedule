package tool.xfy9326.schedule.ui.vm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.content.base.WebCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.kt.MutableEventLiveData
import tool.xfy9326.schedule.kt.postEvent
import tool.xfy9326.schedule.ui.vm.base.AbstractViewModel
import tool.xfy9326.schedule.utils.CourseManager
import tool.xfy9326.schedule.utils.ScheduleManager
import java.util.concurrent.atomic.AtomicBoolean

class WebCourseProviderViewModel : AbstractViewModel() {
    lateinit var importConfig: CourseImportConfig<WebCourseProvider, WebCourseParser>
        private set
    lateinit var courseProvider: WebCourseProvider
        private set
    private lateinit var courseParser: WebCourseParser

    var isBottomPanelInitShowed = false

    // Import success / Has conflicts
    val courseImportFinish = MutableEventLiveData<Pair<Boolean, Boolean>>()
    val providerError = MutableEventLiveData<CourseAdapterException>()
    val validateHtmlPage = MutableEventLiveData<Triple<ImportParams, Boolean, Int>?>()

    private val validatePageLock = Mutex()
    private val isImportingCourses = AtomicBoolean(false)
    private var importCourseJob: Job? = null

    fun registerConfig(config: CourseImportConfig<WebCourseProvider, WebCourseParser>) {
        if (!::importConfig.isInitialized || importConfig != config) {
            importConfig = config
            courseProvider = config.newProvider()
            courseParser = config.newParser()
        }
    }

    fun validateHtmlPage(importParams: ImportParams, currentSchedule: Boolean) {
        if (validatePageLock.tryLock()) {
            viewModelScope.launch {
                try {
                    val pageInfo = courseProvider.validateCourseImportPage(
                        importParams.htmlContent,
                        importParams.iframeContent,
                        importParams.frameContent
                    )
                    if (pageInfo.isValidPage) {
                        validateHtmlPage.postEvent(Triple(importParams, currentSchedule, pageInfo.asImportOption))
                    } else {
                        validateHtmlPage.postEvent(null)
                    }
                } catch (e: CourseAdapterException) {
                    validateHtmlPage.postEvent(null)
                    providerError.postEvent(e)
                } catch (e: Exception) {
                    validateHtmlPage.postEvent(null)
                    providerError.postEvent(CourseAdapterException.ErrorType.UNKNOWN_ERROR.make(e))
                } finally {
                    validatePageLock.unlock()
                }
            }
        }
    }

    fun importCourse(importParams: ImportParams, importOption: Int, currentSchedule: Boolean, newScheduleName: String?) {
        if (isImportingCourses.compareAndSet(false, true)) {
            importCourseJob = viewModelScope.launch {
                try {
                    val scheduleTimes = courseParser.loadScheduleTimes(importOption)
                    val courses = courseParser.parseCourses(
                        importOption,
                        importParams.htmlContent,
                        importParams.iframeContent,
                        importParams.frameContent
                    )

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
                    if (e !is CancellationException) {
                        courseImportFinish.postEvent(false to false)
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
        super.onCleared()
    }

    class ImportParams(val htmlContent: String, val iframeContent: Array<String>, val frameContent: Array<String>)
}