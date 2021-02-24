package tool.xfy9326.schedule.content.base

import tool.xfy9326.schedule.content.beans.WebPageInfo
import java.io.Serializable

abstract class WebCourseProvider<P : Serializable> : AbstractCourseProvider<P>() {
    val initPageUrl: String
        get() = onLoadInitPage()

    fun validateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onValidateCourseImportPage(htmlContent, iframeContent, frameContent)

    protected abstract fun onLoadInitPage(): String

    protected abstract fun onValidateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>): WebPageInfo

}