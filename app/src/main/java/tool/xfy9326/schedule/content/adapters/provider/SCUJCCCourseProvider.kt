package tool.xfy9326.schedule.content.adapters.provider

import org.jsoup.Jsoup
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.beans.WebPageInfo

class SCUJCCCourseProvider : WebCourseProvider<Nothing>() {
    override fun onLoadInitPage(): String = "http://jwweb.scujcc.cn/default2.aspx"

    override fun onValidateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>): WebPageInfo {
        for (html in iframeContent) {
            Jsoup.parse(html).body().getElementById("Table6")?.let {
                return WebPageInfo(true, providedContent = it.outerHtml())
            }
        }
        return WebPageInfo(false)
    }
}