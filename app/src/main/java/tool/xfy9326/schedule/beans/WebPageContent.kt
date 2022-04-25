@file:Suppress("unused")

package tool.xfy9326.schedule.beans

import io.github.xfy9326.atools.core.EMPTY
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report

data class WebPageContent(
    val htmlContent: String = EMPTY,
    val iframeContent: Array<String> = emptyArray(),
    val frameContent: Array<String> = emptyArray(),
    val providedContent: Array<String> = emptyArray(),
) {

    fun requireFirstProvidedContent(): String =
        providedContent.firstOrNull() ?: CourseAdapterException.Error.PARSE_PAGE_ERROR.report()

    fun requireProvidedContentWithMinimumSize(minSize: Int): Array<String> =
        if (providedContent.size < minSize) {
            CourseAdapterException.Error.PARSE_PAGE_ERROR.report()
        } else {
            providedContent
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WebPageContent) return false

        if (htmlContent != other.htmlContent) return false
        if (!iframeContent.contentEquals(other.iframeContent)) return false
        if (!frameContent.contentEquals(other.frameContent)) return false
        if (!providedContent.contentEquals(other.providedContent)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = htmlContent.hashCode()
        result = 31 * result + iframeContent.contentHashCode()
        result = 31 * result + frameContent.contentHashCode()
        result = 31 * result + providedContent.contentHashCode()
        return result
    }
}