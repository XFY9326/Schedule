@file:Suppress("unused")

package tool.xfy9326.schedule.utils

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.content.base.JSCourseProvider
import tool.xfy9326.schedule.kt.NEW_LINE

object JSBridge {
    private const val FUN_HEAD = "pureSchedule_"

    private const val V_CONSOLE_URL = "https://cdn.bootcdn.net/ajax/libs/vConsole/3.4.1/vconsole.min.js"
    const val V_CONSOLE_INJECT =
        """
        javascript:
        (function (window, undefined) {
            let ${FUN_HEAD}CONSOLE_ELEMENT = document.createElement("script");
            ${FUN_HEAD}CONSOLE_ELEMENT.src = "$V_CONSOLE_URL";
            ${FUN_HEAD}CONSOLE_ELEMENT.onload = function() { 
                 window.vConsole = new VConsole();
             };
             document.body.appendChild(${FUN_HEAD}CONSOLE_ELEMENT);
         })(window);
    """

    private const val JS_ENV_BACKUP =
        """
        var PURE_SCHEDULE_ENV_BACKUP_LIST = [];
        for (var PURE_SCHEDULE_ENV_BACKUP_NAME in this) {
            PURE_SCHEDULE_ENV_BACKUP_LIST[PURE_SCHEDULE_ENV_BACKUP_NAME] = this[PURE_SCHEDULE_ENV_BACKUP_NAME];
        }
    """
    private const val JS_ENV_RECOVER =
        """
        for (var PURE_SCHEDULE_ENV_BACKUP_NAME in PURE_SCHEDULE_ENV_BACKUP_LIST) {
            if (this[PURE_SCHEDULE_ENV_BACKUP_NAME] !== PURE_SCHEDULE_ENV_BACKUP_LIST[PURE_SCHEDULE_ENV_BACKUP_NAME]) {
                this[PURE_SCHEDULE_ENV_BACKUP_NAME] = PURE_SCHEDULE_ENV_BACKUP_LIST[PURE_SCHEDULE_ENV_BACKUP_NAME];
            }
        }
        var PURE_SCHEDULE_ENV_BACKUP_LIST = [];
    """

    private const val FUNCTION_NAME_HTML_LOADER = "${FUN_HEAD}htmlContentLoader"
    private const val FUNCTION_HTML_LOADER = """
        function $FUNCTION_NAME_HTML_LOADER() {
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

    fun buildWebCourseProviderJS(isCurrentSchedule: Boolean): String {
        val htmlContent = "${FUN_HEAD}HtmlContent"
        val htmlParam = "$htmlContent[\"html\"]"
        val iframeListParam = "$htmlContent[\"iframe\"]"
        val frameListParam = "$htmlContent[\"frame\"]"
        return """
            javascript:
            $JS_ENV_BACKUP
            (function (window, undefined) {
                $FUNCTION_HTML_LOADER
                
                let $htmlContent = $FUNCTION_NAME_HTML_LOADER();
                
                window.$WEB_COURSE_PROVIDER_JS_INTERFACE_NAME.$WEB_COURSE_PROVIDER_JS_FUNCTION_NAME($htmlParam, $iframeListParam, $frameListParam, $isCurrentSchedule);
            })(window);
            $JS_ENV_RECOVER
        """.trimIndent()
    }

    const val JS_COURSE_PROVIDER_JS_INTERFACE_NAME = "JSCourseProvider"
    private const val JS_COURSE_PROVIDER_JS_FUNCTION_NAME = "onJSProviderResponse"

    suspend fun buildJSCourseProviderJS(isCurrentSchedule: Boolean, jsCourseProvider: JSCourseProvider): String {
        val htmlContent = "${FUN_HEAD}HtmlContent"
        val htmlParam = "$htmlContent[\"html\"]"
        val iframeListParam = "$htmlContent[\"iframe\"]"
        val frameListParam = "$htmlContent[\"frame\"]"
        return """
           javascript:
           $JS_ENV_BACKUP
           (function (window, undefined) {
                let ${FUN_HEAD}LoadJSResult = {
                    "isSuccess": false,
                    "data": "JS launch failed!"
                }
     
                try {
                    ${jsCourseProvider.getJSDependencies().joinToString(NEW_LINE)}
                    
                    ${jsCourseProvider.getJSProvider()}
                
                    ${jsCourseProvider.getJSParser()}
                    
                    $FUNCTION_HTML_LOADER
                    
                    let $htmlContent = $FUNCTION_NAME_HTML_LOADER();
                    
                    let ${FUN_HEAD}ProviderResult = ${jsCourseProvider.getProviderFunctionCallGenerator().invoke(htmlParam, iframeListParam, frameListParam)};
                    
                    let ${FUN_HEAD}ParserResult = ${jsCourseProvider.getParserFunctionCallGenerator().invoke("${FUN_HEAD}ProviderResult")};
                    
                    ${FUN_HEAD}LoadJSResult["isSuccess"] = true;
                    ${FUN_HEAD}LoadJSResult["data"] = JSON.stringify(${FUN_HEAD}ParserResult);
                } catch (err) {
                    ${FUN_HEAD}LoadJSResult["isSuccess"] = false;
                    ${FUN_HEAD}LoadJSResult["data"] = err.toString();
                }
                
                window.$JS_COURSE_PROVIDER_JS_INTERFACE_NAME.$JS_COURSE_PROVIDER_JS_FUNCTION_NAME(JSON.stringify(${FUN_HEAD}LoadJSResult), $isCurrentSchedule)
           })(window);
           $JS_ENV_RECOVER
        """.trimIndent()
    }

    @Serializable
    class JSProviderResponse(val isSuccess: Boolean, val data: String)

    @Keep
    interface WebCourseProviderJSInterface {
        @Keep
        fun onReadHtmlContent(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, isCurrentSchedule: Boolean)
    }

    @Keep
    interface JSCourseProviderJSInterface {
        @Keep
        fun onJSProviderResponse(resultJSON: String, isCurrentSchedule: Boolean)
    }
}