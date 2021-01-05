@file:Suppress("unused")

package tool.xfy9326.schedule.content.base

/**
 * Web course provider
 *
 * @constructor Create empty Web course provider
 */
abstract class WebCourseProvider : ICourseProvider {
    /**
     * Init page url
     */
    abstract val initPageUrl: String

    fun validateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>) =
        onValidateCourseImportPage(htmlContent, iframeContent, frameContent)

    /**
     * On validate course import page
     *
     * @param htmlContent Courses main html
     * @param iframeContent Courses iframeContent html array
     * @param frameContent Courses frameContent html array
     * @return Page info
     */
    protected abstract fun onValidateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>): PageInfo

    /**
     * Page info
     *
     * @property isValidPage Is this page useful
     * @property asImportOption Set as import option, Default: 0
     * @constructor Create empty Page info
     */
    class PageInfo(val isValidPage: Boolean, val asImportOption: Int = 0)
}