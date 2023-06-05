package tool.xfy9326.schedule.ui.vm.base

import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider

abstract class AbstractWebCourseProviderViewModel<I, T1 : AbstractCourseProvider<*>, T2 : AbstractCourseParser<*>> :
    CourseProviderViewModel<I, T1, T2>() {
    val authorName
        get() = importConfigInstance.authorName
    val initPageUrl: String
        get() = getInitUrl()

    protected abstract fun getInitUrl(): String
}