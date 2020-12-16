@file:Suppress("unused")

package tool.xfy9326.schedule.content.base

abstract class WebCourseProvider : ICourseProvider {
    abstract val initPageUrl: String

    fun validateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onValidateCourseImportPage(htmlContent, iframeContent, frameContent)

    protected abstract fun onValidateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>): PageInfo

    class PageInfo(val isValidPage: Boolean, val asImportOption: Int = 0)
}