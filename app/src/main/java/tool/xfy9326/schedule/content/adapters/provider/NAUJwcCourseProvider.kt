package tool.xfy9326.schedule.content.adapters.provider

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.beans.LoginPageInfo
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import tool.xfy9326.schedule.content.utils.md5

class NAUJwcCourseProvider : LoginCourseProvider<Nothing>() {
    companion object {
        private const val JWC_HOST = "jwc.nau.edu.cn"
        private const val JWC_URL = "http://$JWC_HOST"
        private const val JWC_LOGIN_URL = "http://$JWC_HOST/NauEventHandle.ashx?class=LoginHandle&meth=DoLogin"
        private const val JWC_LOGOUT_URL = "http://$JWC_HOST/Students/NauEventHandle.ashx?class=LoginHandle&meth=LogOut"

        private const val JWC_COURSE_THIS_TERM_URL = "http://$JWC_HOST/Students/MyCourseScheduleTable.aspx"
        private const val JWC_COURSE_NEXT_TERM_URL = "http://$JWC_HOST/Students/MyCourseScheduleTableNext.aspx"

        private const val SELECTOR_CHECK_CODE = "#login > ul > li:nth-child(3) > img"

        private const val JWC_URL_PARAM_D = "d"

        private const val HTML_ATTR_SRC = "src"

        private const val LOGIN_PARAMS_NAME = "para"
        private const val LOGIN_PARAMS_VALUE = "%s;%s;%s;%s;202020212"

        private const val USERNAME_ERROR = "账户不存在"
        private const val PASSWORD_ERROR = "密码错误"
        private const val LOGIN_SUCCESS = "登陆成功"

        @Serializable
        private data class JwcLoginResponse(
            val Success: String,
            val Message: String,
            val RedirectPath: String,
        )
    }

    override val enableCaptcha = true

    override fun onPrepareClient(): HttpClient = CourseAdapterUtils.buildSimpleHttpClient(supportJson = true)

    override suspend fun onLoadLoginPage(importOption: Int): LoginPageInfo {
        val content = httpClient.get<String>(JWC_URL)
        val checkCodeImgTag = Jsoup.parse(content).body().selectFirst(SELECTOR_CHECK_CODE)
        checkCodeImgTag.setBaseUri(JWC_URL)
        return LoginPageInfo(checkCodeImgTag.absUrl(HTML_ATTR_SRC))
    }

    override suspend fun onLogin(userId: String, userPw: String, captchaCode: String?, loginPageInfo: LoginPageInfo, importOption: Int) {
        if (captchaCode == null) CourseAdapterException.Error.CAPTCHA_CODE_ERROR.report()

        val loginResponseText = httpClient.submitForm<String>(
            JWC_LOGIN_URL,
            Parameters.build {
                append(LOGIN_PARAMS_NAME, LOGIN_PARAMS_VALUE.format(userId, userPw.md5(), captchaCode, "$userId$userPw$captchaCode".md5()))
            }
        )
        val loginResponse = Json.decodeFromString<JwcLoginResponse>(loginResponseText)

        if (loginResponse.Success == "1" && loginResponse.RedirectPath.isNotBlank() && LOGIN_SUCCESS in loginResponse.Message) {
            val homeResponse = httpClient.get<HttpResponse>("$JWC_URL/${loginResponse.RedirectPath}")
            if (homeResponse.request.url.parameters[JWC_URL_PARAM_D] == null) {
                CourseAdapterException.Error.LOGIN_SERVER_ERROR.report()
            }
        } else {
            when {
                USERNAME_ERROR in loginResponse.Message -> CourseAdapterException.Error.USER_ID_ERROR.report()
                PASSWORD_ERROR in loginResponse.Message -> CourseAdapterException.Error.USER_PASSWORD_ERROR.report()
                else -> CourseAdapterException.Error.CUSTOM_ERROR.report(msg = loginResponse.Message)
            }
        }
    }

    private suspend fun logout() {
        httpClient.get<Unit>(JWC_LOGOUT_URL)
    }

    override suspend fun onLoadCoursesHtml(importOption: Int): String =
        when (importOption) {
            0 -> httpClient.get(JWC_COURSE_THIS_TERM_URL)
            1 -> httpClient.get(JWC_COURSE_NEXT_TERM_URL)
            else -> CourseAdapterException.Error.IMPORT_SELECT_OPTION_ERROR.report()
        }

    override suspend fun onClearConnection() {
        logout()
    }
}