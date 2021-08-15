package tool.xfy9326.schedule.beans

import lib.xfy9326.kit.EMPTY

data class WebPageContent(
    val htmlContent: String = EMPTY,
    val iframeContent: Array<String> = emptyArray(),
    val frameContent: Array<String> = emptyArray(),
    val providedContent: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WebPageContent) return false

        if (htmlContent != other.htmlContent) return false
        if (!iframeContent.contentEquals(other.iframeContent)) return false
        if (!frameContent.contentEquals(other.frameContent)) return false
        if (providedContent != other.providedContent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = htmlContent.hashCode()
        result = 31 * result + iframeContent.contentHashCode()
        result = 31 * result + frameContent.contentHashCode()
        result = 31 * result + (providedContent?.hashCode() ?: 0)
        return result
    }
}