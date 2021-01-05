package tool.xfy9326.schedule.content.adapters.provider

import tool.xfy9326.schedule.content.base.WebCourseProvider

class NAUWebCourseProvider : WebCourseProvider() {
    companion object {
        private const val PAGE_TEXT = "在修课程课表"
        private const val COURSE_TEXT_1 = "MyCourseScheduleTable.aspx"
        private const val COURSE_TEXT_2 = "MyCourseScheduleTableNext.aspx"
    }

    override fun onLoadInitPage(): String = "http://my.nau.edu.cn"

    override fun onValidateCourseImportPage(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>): PageInfo {
        if (COURSE_TEXT_1 in htmlContent) {
            for (content in iframeContent) {
                if (PAGE_TEXT in content) return PageInfo(true, 0)
            }
        } else if (COURSE_TEXT_2 in htmlContent) {
            for (content in iframeContent) {
                if (PAGE_TEXT in content) return PageInfo(true, 1)
            }
        }
        return PageInfo(false)
    }
}