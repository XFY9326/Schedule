package tool.xfy9326.schedule.utils

import androidx.annotation.Keep
import io.github.xfy9326.atools.base.EMPTY
import kotlinx.serialization.Serializable
import tool.xfy9326.schedule.content.js.JSCourseProvider

object JSBridge {
    private const val FUN_HEAD_PREFIX = "PureSchedule_"
    private const val FUN_HEAD_RANDOM_LENGTH = 8
    private val FUN_HEAD by lazy {
        val randomCharset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomStr = Array(FUN_HEAD_RANDOM_LENGTH) { randomCharset.random() }.joinToString("")
        FUN_HEAD_PREFIX + randomStr + "_"
    }
    const val SCRIPT_EXECUTE_SUCCESS_RESULT = "\"Done\""

    @Suppress("SpellCheckingInspection")
    private const val V_CONSOLE_URL = "https://unpkg.com/vconsole@latest/dist/vconsole.min.js"
    val V_CONSOLE_INJECT =
        """
        javascript:
        (function(window, undefined) {
            let ${FUN_HEAD}CONSOLE_ELEMENT = document.createElement("script");
            ${FUN_HEAD}CONSOLE_ELEMENT.src = "$V_CONSOLE_URL";
            ${FUN_HEAD}CONSOLE_ELEMENT.onload = function() { 
                 window.vConsole = new VConsole();
             };
             document.body.appendChild(${FUN_HEAD}CONSOLE_ELEMENT);
        })(window);
        $SCRIPT_EXECUTE_SUCCESS_RESULT
    """.trimIndent()

    private val FUNCTION_NAME_HTML_LOADER = "${FUN_HEAD}htmlContentLoader"
    private val FUNCTION_HTML_LOADER = """
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
    """.trimIndent()

    private val FUNCTION_NAME_EXCEPTION_DUMP = "${FUN_HEAD}exceptionDump"
    private val FUNCTION_EXCEPTION_DUMP = """
        function $FUNCTION_NAME_EXCEPTION_DUMP(err) {
            if (err.hasOwnProperty("stack")) {
                return err.stack.toString();
            } else {
                return err.toString();
            }
        }
    """.trimIndent()


    const val WEB_COURSE_PROVIDER_JS_INTERFACE_NAME = "WebCourseProvider"
    private const val WEB_COURSE_PROVIDER_JS_FUNCTION_NAME = "onReadHtmlContent"

    fun buildWebCourseProviderJS(isCurrentSchedule: Boolean): String {
        val htmlContent = "${FUN_HEAD}HtmlContent"
        val htmlParam = "$htmlContent[\"html\"]"
        val iframeListParam = "$htmlContent[\"iframe\"]"
        val frameListParam = "$htmlContent[\"frame\"]"
        return """
            javascript:
            (function(window, undefined) {
                $FUNCTION_HTML_LOADER
                
                let $htmlContent = $FUNCTION_NAME_HTML_LOADER();
                
                window.$WEB_COURSE_PROVIDER_JS_INTERFACE_NAME.$WEB_COURSE_PROVIDER_JS_FUNCTION_NAME($htmlParam, $iframeListParam, $frameListParam, $isCurrentSchedule);
            })(window);
            $SCRIPT_EXECUTE_SUCCESS_RESULT
        """.trimIndent()
    }

    const val JS_COURSE_PROVIDER_JS_INTERFACE_NAME = "JSCourseProvider"
    private const val JS_COURSE_PROVIDER_JS_FUNCTION_NAME = "onJSProviderResponse"

    suspend fun buildJSCourseProviderJS(isCurrentSchedule: Boolean, jsCourseProvider: JSCourseProvider, isDebug: Boolean): String {
        val htmlContent = "${FUN_HEAD}HtmlContent"
        val htmlParam = "$htmlContent[\"html\"]"
        val iframeListParam = "$htmlContent[\"iframe\"]"
        val frameListParam = "$htmlContent[\"frame\"]"
        val functionType = if (jsCourseProvider.isAsyncEnvironment()) "async function" else "function"
        val callFunctionAwait = if (jsCourseProvider.isAsyncEnvironment()) "await" else EMPTY
        val providerFunctionName = "${FUN_HEAD}Provider"
        val parserFunctionName = "${FUN_HEAD}Parser"

        val rawProviderFunctionName = jsCourseProvider.getProviderFunctionName()
        val rawParserFunctionName = jsCourseProvider.getParserFunctionName()
        val providerFunctionCall = jsCourseProvider.getProviderFunctionCallGenerator(providerFunctionName)
        val parserFunctionCall = jsCourseProvider.getParserFunctionCallGenerator(parserFunctionName)

        val responseFunctionName = "${FUN_HEAD}response"

        return """
           javascript:
           ($functionType(window, undefined) {
                function $responseFunctionName(success, data) {
                    let outputContent = JSON.stringify({
                        "isSuccess": success,
                        "data": data
                    });
                    window.$JS_COURSE_PROVIDER_JS_INTERFACE_NAME.$JS_COURSE_PROVIDER_JS_FUNCTION_NAME(outputContent, $isCurrentSchedule)
                }
                
                $FUNCTION_EXCEPTION_DUMP
                $FUNCTION_HTML_LOADER
                
                ${if (isDebug) "console.log('Loading dependencies');" else EMPTY}
                try {
                    ${jsCourseProvider.getJSDependencies().joinToString(NEW_LINE)}
                } catch (err) {
                    ${if (isDebug) "console.error(err);" else EMPTY}
                    $responseFunctionName(false, "Dependencies error:\n" + $FUNCTION_NAME_EXCEPTION_DUMP(err));
                    return;
                }
                
                ${if (isDebug) "console.log('Loading html content');" else EMPTY}
                let $htmlContent;
                try {
                    $htmlContent = $FUNCTION_NAME_HTML_LOADER();
                } catch (err) {
                    ${if (isDebug) "console.error(err);" else EMPTY}
                    $responseFunctionName(false, "Html loader error:\n" + $FUNCTION_NAME_EXCEPTION_DUMP(err));
                    return;
                }
                
                ${if (isDebug) "console.log('Loading provider result');" else EMPTY}
                let ${FUN_HEAD}ProviderResult;
                try {
                    let $providerFunctionName = $callFunctionAwait ($functionType(window, undefined) {
                        ${jsCourseProvider.getJSProvider()}
                        if (typeof $rawProviderFunctionName !== 'function') throw TypeError("Can't find function '$rawProviderFunctionName'!");
                        return $rawProviderFunctionName;
                    })(window);
                    ${FUN_HEAD}ProviderResult = $callFunctionAwait ${providerFunctionCall(htmlParam, iframeListParam, frameListParam)};
                } catch (err) {
                    ${if (isDebug) "console.error(err);" else EMPTY}
                    $responseFunctionName(false, "Provider error:\n" + $FUNCTION_NAME_EXCEPTION_DUMP(err));
                    return;
                }
                ${if (isDebug) "console.log(${FUN_HEAD}ProviderResult);" else EMPTY}
                
                ${if (isDebug) "console.log('Loading parser result');" else EMPTY}
                let ${FUN_HEAD}ParserResult;
                try {
                    let $parserFunctionName = $callFunctionAwait ($functionType(window, undefined) {
                        ${jsCourseProvider.getJSParser()}
                        if (typeof $rawParserFunctionName !== 'function') throw TypeError("Can't find function '$rawParserFunctionName'!");
                        return $rawParserFunctionName;
                    })(window);
                    ${FUN_HEAD}ParserResult = $callFunctionAwait ${parserFunctionCall("${FUN_HEAD}ProviderResult")};
                } catch (err) {
                    ${if (isDebug) "console.error(err);" else EMPTY}
                    $responseFunctionName(false, "Parser error:\n" + $FUNCTION_NAME_EXCEPTION_DUMP(err));
                    return;
                }
                ${if (isDebug) "console.log(${FUN_HEAD}ParserResult);" else EMPTY}
                
                $responseFunctionName(true, JSON.stringify(${FUN_HEAD}ParserResult));
           })(window);
           $SCRIPT_EXECUTE_SUCCESS_RESULT
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