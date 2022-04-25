package tool.xfy9326.schedule.content.adapters.provider

import io.github.xfy9326.atools.core.toHex
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.beans.LoginPageInfo
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.runResponseCatching
import tool.xfy9326.schedule.content.utils.selectSingle
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

class NAUJwcCourseProvider : LoginCourseProvider<Nothing>() {
    companion object {
        private const val JWC_HOST = "jwc.nau.edu.cn"
        private const val SSO_HOST = "sso.nau.edu.cn"
        private const val JWC_URL = "http://$JWC_HOST"
        private const val SSO_URL = "http://$SSO_HOST"

        @Suppress("SpellCheckingInspection")
        private const val JWC_LOGIN_URL = "$SSO_URL/sso/login?service=http%3A%2F%2Fjwc.nau.edu.cn%2Flogin_single.aspx"
        private const val JWC_LOGOUT_URL = "$JWC_URL/Students/NauEventHandle.ashx?class=LoginHandle&meth=LogOut"
        private const val SSO_LOGOUT_URL = "$SSO_URL/sso/logout"

        private const val JWC_COURSE_THIS_TERM_URL = "$JWC_URL/Students/MyCourseScheduleTable.aspx"
        private const val JWC_COURSE_NEXT_TERM_URL = "$JWC_URL/Students/MyCourseScheduleTableNext.aspx"

        private const val SSO_INPUT_TAG_NAME_ATTR = "name"
        private const val SSO_INPUT_TAG_VALUE_ATTR = "value"
        private const val SELECTOR_LOGIN_FORM = "#fm1"
        private const val SELECTOR_POST_FORMAT = "input[$SSO_INPUT_TAG_NAME_ATTR=%s]"
        private const val SELECTOR_CHECK_CODE = "$SELECTOR_LOGIN_FORM > div:nth-child(4) > img"
        private const val SELECTOR_LOGIN_ERROR_MSG = "#msg1"

        private const val HTML_ATTR_SRC = "src"

        private const val LOGIN_PARAMS_USER_NAME = "username"
        private const val LOGIN_PARAMS_PASSWORD = "password"
        private const val LOGIN_PARAMS_AUTH_CODE = "authcode"
        private val LOGIN_PARAMS_STATIC_ARRAY = arrayOf("execution", "encrypted", "_eventId", "loginType", "submit")

        private const val LOGIN_PAGE_CONTENT = "统一身份认证登录"

        const val IMPORT_OPTION_THIS_TERM = 0
        const val IMPORT_OPTION_NEXT_TERM = 1

        private const val LOGIN_PASSWORD_ALGORITHM_NAME = "RSA"
        private const val LOGIN_PASSWORD_EXPONENT = "010001"

        @Suppress("SpellCheckingInspection")
        private const val LOGIN_PASSWORD_MODULUS =
            "008aed7e057fe8f14c73550b0e6467b023616ddc8fa91846d2613cdb7f7621e3cada4cd5d812d627af6b87727ade4e26d26208b7326815941492b2204c3167ab2d53df1e3a2c9153bdb7c8c2e968df97a5e7e01cc410f92c4c2c2fba529b3ee988ebc1fca99ff5119e036d732c368acf8beba01aa2fdafa45b21e4de4928d0d403"

        private fun encryptPassword(password: String): String {
            val modulus = BigInteger(LOGIN_PASSWORD_MODULUS, 16)
            val exponent = BigInteger(LOGIN_PASSWORD_EXPONENT, 16)

            val keyFactory = KeyFactory.getInstance(LOGIN_PASSWORD_ALGORITHM_NAME)
            val publicKey = keyFactory.generatePublic(RSAPublicKeySpec(modulus, exponent)) as RSAPublicKey

            val cipher = Cipher.getInstance(LOGIN_PASSWORD_ALGORITHM_NAME)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            return cipher.doFinal(password.reversed().toByteArray()).toHex()
        }
    }

    private var studentDefaultPageUrl: Url? = null

    override val enableCaptcha = true

    override suspend fun onLoadLoginPage(importOption: Int): LoginPageInfo {
        var content: String = httpClient.get(JWC_LOGIN_URL).bodyAsText()
        if (LOGIN_PAGE_CONTENT !in content) {
            logout()
            content = httpClient.get(JWC_LOGIN_URL).bodyAsText()
        }
        val htmlContent = Jsoup.parse(content, SSO_URL).body()
        val captchaUrl = htmlContent.selectSingle(SELECTOR_CHECK_CODE).absUrl(HTML_ATTR_SRC)
        val loginFormElement = htmlContent.selectSingle(SELECTOR_LOGIN_FORM)
        var loginParams = Parameters.Empty
        for (param in LOGIN_PARAMS_STATIC_ARRAY) {
            val input = loginFormElement.selectSingle(SELECTOR_POST_FORMAT.format(param))
            val value = input.attr(SSO_INPUT_TAG_VALUE_ATTR)
            loginParams += parametersOf(param, value)
        }
        return LoginPageInfo(captchaUrl = captchaUrl, loginParams = loginParams)
    }

    override suspend fun onLogin(userId: String, userPw: String, captchaCode: String?, loginPageInfo: LoginPageInfo, importOption: Int) {
        if (captchaCode == null) CourseAdapterException.Error.CAPTCHA_CODE_ERROR.report()
        var loginParams = loginPageInfo.loginParams ?: Parameters.Empty
        loginParams += parametersOf(LOGIN_PARAMS_USER_NAME, userId)
        loginParams += parametersOf(LOGIN_PARAMS_PASSWORD, encryptPassword(userPw))
        loginParams += parametersOf(LOGIN_PARAMS_AUTH_CODE, captchaCode)
        val loginResponse = httpClient.runResponseCatching(
            action = { submitForm(JWC_LOGIN_URL, loginParams) },
            handleError = {
                CourseAdapterException.Error.LOGIN_SERVER_ERROR.report(msg = "Response: $this\nContent: \n${response.bodyAsText()}")
            }
        )
        val loginResponseUrl = loginResponse.request.url
        when (loginResponseUrl.host) {
            JWC_HOST -> studentDefaultPageUrl = loginResponseUrl
            SSO_HOST -> {
                val loginResponseContent = loginResponse.bodyAsText()
                val msgElement = Jsoup.parse(loginResponseContent).body().selectSingle(SELECTOR_LOGIN_ERROR_MSG)
                CourseAdapterException.reportCustomError(msgElement.text())
            }
            else -> CourseAdapterException.Error.UNKNOWN_ERROR.report()
        }
    }

    private suspend fun logout() {
        httpClient.get(JWC_LOGOUT_URL)
        httpClient.get(SSO_LOGOUT_URL)
        studentDefaultPageUrl = null
    }

    override suspend fun onLoadCoursesHtml(importOption: Int): String =
        when (importOption) {
            IMPORT_OPTION_THIS_TERM -> httpClient.get(JWC_COURSE_THIS_TERM_URL).bodyAsText()
            IMPORT_OPTION_NEXT_TERM -> httpClient.get(JWC_COURSE_NEXT_TERM_URL).bodyAsText()
            else -> CourseAdapterException.Error.IMPORT_SELECT_OPTION_ERROR.report()
        }

    override suspend fun onLoadTermHtml(importOption: Int): String? =
        when (importOption) {
            IMPORT_OPTION_THIS_TERM -> studentDefaultPageUrl?.let { httpClient.get(it).bodyAsText() }
            IMPORT_OPTION_NEXT_TERM -> null
            else -> CourseAdapterException.Error.IMPORT_SELECT_OPTION_ERROR.report()
        }

    override suspend fun onClearConnection() {
        logout()
        super.onClearConnection()
    }
}