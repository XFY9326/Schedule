@file:Suppress("unused")

package tool.xfy9326.schedule.utils

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.content.base.JSCourseProvider

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

    fun buildWebCourseProviderJS(isCurrentSchedule: Boolean) =
        """
            javascript:
            {
                $FUNCTION_HTML_LOADER
                let htmlContent = $FUNCTION_NAME_HTML_LOADER();
                window.$WEB_COURSE_PROVIDER_JS_INTERFACE_NAME.$WEB_COURSE_PROVIDER_JS_FUNCTION_NAME(htmlContent["html"], htmlContent["iframe"], htmlContent["frame"], $isCurrentSchedule);
            }
        """.trimIndent()

    const val JS_COURSE_PROVIDER_JS_INTERFACE_NAME = "JSCourseProvider"
    private const val JS_COURSE_PROVIDER_JS_FUNCTION_NAME = "onGetJSResult"

    suspend fun buildJSCourseProviderJS(isCurrentSchedule: Boolean, jsCourseProvider: JSCourseProvider): String {
        val funHead = "pureSchedule_"
        val htmlContent = "${funHead}HtmlContent"
        val htmlParam = "$htmlContent[\"html\"]"
        val iframeListParam = "$htmlContent[\"iframe\"]"
        val frameListParam = "$htmlContent[\"frame\"]"
        return """
           javascript:
           {
                let ${funHead}LoadJSResult = {
                    "isSuccess": false,
                    "data": "JS Start failed!"
                }
                
                try {
                    ${jsCourseProvider.getJSDependencies().joinToString("\n")}
                
                    ${jsCourseProvider.getJSProvider()}
                
                    ${jsCourseProvider.getJSParser()}
                    
                    $FUNCTION_HTML_LOADER
                    let $htmlContent = $FUNCTION_NAME_HTML_LOADER();
                    
                    let ${funHead}ProviderResult = ${jsCourseProvider.providerFunctionCallGenerator.invoke(htmlParam, iframeListParam, frameListParam)};
                    
                    let ${funHead}ParserResult = ${jsCourseProvider.parserFunctionCallGenerator.invoke("${funHead}ProviderResult")};
                    
                    ${funHead}LoadJSResult["isSuccess"] = true;
                    ${funHead}LoadJSResult["data"] = JSON.stringify(${funHead}ParserResult);
                } catch (err) {
                    ${funHead}LoadJSResult["isSuccess"] = false;
                    ${funHead}LoadJSResult["data"] = err.toString();
                }
                
                window.$JS_COURSE_PROVIDER_JS_INTERFACE_NAME.$JS_COURSE_PROVIDER_JS_FUNCTION_NAME(${funHead}LoadJSResult, $isCurrentSchedule)
           }
        """.trimIndent()
    }

    @Serializable
    data class JSResult(val isSuccess: Boolean, val data: String)

    @Keep
    interface WebCourseProviderJSInterface {
        @Keep
        fun onReadHtmlContent(htmlContent: String, iframeContent: Array<String>, frameContent: Array<String>, isCurrentSchedule: Boolean)
    }

    @Keep
    interface JSCourseProviderJSInterface {
        @Keep
        fun onGetJSResult(resultJSON: String, isCurrentSchedule: Boolean)
    }
}