package tool.xfy9326.schedule.content.adapters.provider

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jsoup.Jsoup
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.kt.toHex
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

class NAUSSOCourseProvider : LoginCourseProvider<Nothing>(null) {
    companion object {
        private const val JWC_HOST = "jwc.nau.edu.cn"
        private const val SSO_HOST = "sso.nau.edu.cn"
        private const val JWC_LOGIN_SINGLE_URL = "http://$JWC_HOST/Login_Single.aspx"
        private const val JWC_SSO_LOGIN_URL = "http://$SSO_HOST/sso/login?service=$JWC_LOGIN_SINGLE_URL"

        private const val JWC_LOGOUT_URL = "http://$JWC_HOST/Students/NauEventHandle.ashx?class=LoginHandle&meth=LogOut"
        private const val SSO_LOGOUT_URL = "http://$SSO_HOST/sso/logout"

        private const val JWC_COURSE_THIS_TERM_URL = "http://$JWC_HOST/Students/MyCourseScheduleTable.aspx"
        private const val JWC_COURSE_NEXT_TERM_URL = "http://$JWC_HOST/Students/MyCourseScheduleTableNext.aspx"

        private const val JWC_ALREADY_LOGIN = "已经登录"
        private const val SSO_ACCOUNT_ERROR = "账号被"
        private const val SSO_LOGIN_PASSWORD_ERROR_STR = "密码错误"

        private const val JWC_URL_PARAM_R = "r"
        private const val JWC_URL_PARAM_D = "d"

        private const val HTML_ATTR_NAME = "name"
        private const val HTML_ATTR_VALUE = "value"
        private const val HTML_DIV = "div"

        private const val SSO_USER_PASSWORD_ENCRYPTED = "encrypted"
        private const val SSO_INPUT = "#fm1 > $HTML_DIV:nth-child(5)"
        private const val SSO_POST_FORMAT = "input[$HTML_ATTR_NAME=%s]"
        private const val SSO_POST_USERNAME = "username"
        private const val SSO_POST_PASSWORD = "password"
        private val SSO_LOGIN_PARAM = arrayOf("execution", "_eventId", "loginType", "submit")

        private const val RSA_ALGORITHM_NAME = "RSA"
        private const val RSA_EXPONENT = "010001"

        @Suppress("SpellCheckingInspection")
        private const val RSA_MODULUS =
            "008aed7e057fe8f14c73550b0e6467b023616ddc8fa91846d2613cdb7f7621e3cada4cd5d812d627af6b87727ade4e26d26208b7326815941492b2204c3167ab2d53df1e3a2c9153bdb7c8c2e968df97a5e7e01cc410f92c4c2c2fba529b3ee988ebc1fca99ff5119e036d732c368acf8beba01aa2fdafa45b21e4de4928d0d403"

        private fun encryptPassword(password: String): String {
            val modulus = BigInteger(RSA_MODULUS, 16)
            val exponent = BigInteger(RSA_EXPONENT, 16)

            val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM_NAME)
            val publicKey = keyFactory.generatePublic(RSAPublicKeySpec(modulus, exponent)) as RSAPublicKey

            val cipher = Cipher.getInstance(RSA_ALGORITHM_NAME)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            return cipher.doFinal(password.reversed().toByteArray()).toHex()
        }

        private fun buildPostForm(userId: String, userPw: String, ssoResponseContent: String) = Parameters.build {
            val htmlContent = Jsoup.parse(ssoResponseContent).select(SSO_INPUT)
            append(SSO_POST_USERNAME, userId)

            append(SSO_POST_PASSWORD, encryptPassword(userPw))
            append(SSO_USER_PASSWORD_ENCRYPTED, true.toString())

            for (param in SSO_LOGIN_PARAM) {
                val input = htmlContent.select(SSO_POST_FORMAT.format(param)).first()
                val value = input.attr(HTML_ATTR_VALUE)
                append(param, value)
            }
        }
    }

    override val enableCaptcha = false

    override suspend fun onLogin(httpClient: HttpClient, userId: String, userPw: String, captchaCode: String?, importOption: Int) =
        ssoLogin(httpClient, userId, userPw)

    private suspend fun ssoLogin(httpClient: HttpClient, userId: String, userPw: String, reLogin: Boolean = true) {
        val beforeLoginResponse = httpClient.get<String>(JWC_SSO_LOGIN_URL)
        val loginResponse = httpClient.submitForm<HttpResponse>(
            JWC_SSO_LOGIN_URL,
            buildPostForm(userId, userPw, beforeLoginResponse)
        )

        loginResponse.request.url.apply {
            return when (host) {
                JWC_HOST -> {
                    if (parameters[JWC_URL_PARAM_R].isNullOrBlank() || parameters[JWC_URL_PARAM_D].isNullOrBlank()) {
                        val content = loginResponse.receive<String>()
                        if (JWC_ALREADY_LOGIN in content) {
                            if (reLogin) {
                                logout(httpClient)
                                ssoLogin(httpClient, userId, userPw, false)
                            } else {
                                CourseAdapterException.ErrorType.LOGIN_SERVER_ERROR.report()
                            }
                        } else {
                            CourseAdapterException.ErrorType.UNKNOWN_ERROR.report()
                        }
                    } else {
                        return
                    }
                }
                SSO_HOST -> {
                    val content = loginResponse.receive<String>()
                    when {
                        SSO_LOGIN_PASSWORD_ERROR_STR in content -> CourseAdapterException.ErrorType.USER_ID_OR_PASSWORD_ERROR.report()
                        SSO_ACCOUNT_ERROR in content -> CourseAdapterException.ErrorType.ACCOUNT_ERROR.report()
                        else -> CourseAdapterException.ErrorType.UNKNOWN_ERROR.report()
                    }
                }
                else -> CourseAdapterException.ErrorType.UNKNOWN_ERROR.report()
            }
        }
    }

    private suspend fun logout(httpClient: HttpClient) {
        httpClient.get<Unit>(JWC_LOGOUT_URL)
        httpClient.get<Unit>(SSO_LOGOUT_URL)
    }

    override suspend fun onLoadCoursesHtml(httpClient: HttpClient, importOption: Int): String =
        when (importOption) {
            0 -> httpClient.get(JWC_COURSE_THIS_TERM_URL)
            1 -> httpClient.get(JWC_COURSE_NEXT_TERM_URL)
            else -> CourseAdapterException.ErrorType.IMPORT_SELECT_OPTION_ERROR.report()
        }

    override suspend fun onClearConnection(httpClient: HttpClient) {
        logout(httpClient)
    }
}