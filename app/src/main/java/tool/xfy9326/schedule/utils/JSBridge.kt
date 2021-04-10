@file:Suppress("unused")

package tool.xfy9326.schedule.utils

import androidx.annotation.Keep

object JSBridge {
    private const val FUNCTION_NAME_HTML_LOADER = "htmlContentLoader"
    private const val FUNCTION_HTML_LOADER = """
        function $FUNCTION_NAME_HTML_LOADER(){
            let htmlContent = document.getElementsByTagName("html")[0].outerHTML;
            
            let iframeTags = document.getElementsByTagName("iframe");
            let iframeList = [];
            for (let i = 0; i < iframeTags.length; i++) {
                iframeList.push(iframeTags[i].contentDocument.body.parentElement.outerHTML);
            }
            
            let frameTags = document.getElementsByTagName("frame");
            let frameList = [];
            for (let i = 0; i < frameTags.length; i++) {
                frameList.push(frameTags[i].contentDocument.body.parentElement.outerHTML);
            }
            
            return {
                "html": htmlContent,
                "iframe": iframeList,
                "frame": frameList
            };
        }
    """

    const val WEB_COURSE_PROVIDER_JS_INTERFACE_NAME = "WebCourseProvider"
    private const val WEB_COURSE_PROVIDER_JS_FUNCTION_NAME = "onReadHtmlContent"
    const val WEB_COURSE_PROVIDER_FUNCTION_NAME_SCHEDULE_LOADER = "PureSchedule_ScheduleLoader"
    const val WEB_COURSE_PROVIDER_FUNCTION_SCHEDULE_LOADER = """
        function $WEB_COURSE_PROVIDER_FUNCTION_NAME_SCHEDULE_LOADER(isCurrentSchedule) {
            $FUNCTION_HTML_LOADER
            let htmlContent = $FUNCTION_NAME_HTML_LOADER();
            
            window.$WEB_COURSE_PROVIDER_JS_INTERFACE_NAME.$WEB_COURSE_PROVIDER_JS_FUNCTION_NAME(htmlContent["html"], htmlContent["iframe"], htmlContent["frame"], isCurrentSchedule);
        }
        """

    @Keep
    interface WebCourseProviderJSInterface {
        @Keep
        fun onReadHtmlContent(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, isCurrentSchedule: Boolean)
    }
}