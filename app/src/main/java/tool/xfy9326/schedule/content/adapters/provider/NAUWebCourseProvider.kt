package tool.xfy9326.schedule.content.adapters.provider

import lib.xfy9326.kit.asArray
import tool.xfy9326.schedule.content.base.WebCourseProvider
import tool.xfy9326.schedule.content.beans.WebPageInfo

class NAUWebCourseProvider : WebCourseProvider<Nothing>() {
    companion object {
        private const val PAGE_TEXT = "在修课程课表"
        private const val COURSE_TEXT_1 = "MyCourseScheduleTable.aspx"
        private const val COURSE_TEXT_2 = "MyCourseScheduleTableNext.aspx"
    }

    override fun onLoadInitPage(): String = "http://sso.nau.edu.cn/sso/login?service=http://jwc.nau.edu.cn/login_single.aspx"

    override fun onValidateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>): WebPageInfo {
        if (COURSE_TEXT_1 in htmlContent) {
            for (content in iframeContent) {
                if (PAGE_TEXT in content) return WebPageInfo(true, 0, content.asArray())
            }
        } else if (COURSE_TEXT_2 in htmlContent) {
            for (content in iframeContent) {
                if (PAGE_TEXT in content) return WebPageInfo(true, 1, content.asArray())
            }
        }
        return WebPageInfo(false)
    }
}