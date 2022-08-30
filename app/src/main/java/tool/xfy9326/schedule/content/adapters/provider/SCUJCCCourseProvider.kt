package tool.xfy9326.schedule.content.adapters.provider

import org.jsoup.Jsoup
import tool.xfy9326.schedule.content.adapters.parser.SCUJCCCourseParser
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.beans.WebPageInfo
import tool.xfy9326.schedule.content.utils.selectSingle

class SCUJCCCourseProvider : WebCourseProvider<Nothing>() {
    companion object {
        private const val LOGIN_PAGE = "https://jwweb.cdjcc.edu.cn"
        private const val MAIN_CONTENT_SELECTOR = "div.main_box"
        private const val TAG_ID_TABLE_1 = "Table1" // ImportOption 0
        private const val TAG_ID_TABLE_6 = "Table6" // ImportOption 1
    }

    override fun onLoadInitPage(): String = LOGIN_PAGE

    override fun onValidateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>): WebPageInfo {
        for (html in iframeContent) {
            val mainContent = Jsoup.parse(html).body().selectSingle(MAIN_CONTENT_SELECTOR)
            val content1 = mainContent.getElementById(TAG_ID_TABLE_1)
            if (content1 != null) {
                return WebPageInfo(true, SCUJCCCourseParser.IMPORT_OPTION_TABLE_1, arrayOf(content1.outerHtml()))
            }
            val content6 = mainContent.getElementById(TAG_ID_TABLE_6)
            if (content6 != null) {
                return WebPageInfo(true, SCUJCCCourseParser.IMPORT_OPTION_TABLE_6, arrayOf(content6.outerHtml()))
            }
        }
        return WebPageInfo(false)
    }
}