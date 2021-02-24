package tool.xfy9326.schedule.content.base

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import java.io.Serializable

abstract class LoginCourseProvider<P : Serializable> : NetworkCourseProvider<P>() {
    abstract val enableCaptcha: Boolean

    suspend fun loadCaptchaImage(importOption: Int = 0) =
        if (enableCaptcha) {
            val url = loadCaptchaUrl(importOption)
            if (url == null) {
                CourseAdapterException.Error.CAPTCHA_DOWNLOAD_ERROR.report()
            } else {
                downloadCaptchaImage(url, importOption)
            }
        } else {
            null
        }

    private suspend fun loadCaptchaUrl(importOption: Int) = onLoadCaptchaUrl(requireHttpClient(), importOption)

    private suspend fun downloadCaptchaImage(captchaUrl: String, importOption: Int) =
        try {
            onDownloadCaptchaImage(requireHttpClient(), captchaUrl, importOption)
        } catch (e: Exception) {
            CourseAdapterException.Error.CAPTCHA_DOWNLOAD_ERROR.report(e)
        }

    suspend fun login(userId: String, userPw: String, captchaCode: String?, importOption: Int) =
        onLogin(requireHttpClient(), userId, userPw, captchaCode, importOption)

    protected open suspend fun onLoadCaptchaUrl(httpClient: HttpClient, importOption: Int): String? = null

    protected abstract suspend fun onLogin(httpClient: HttpClient, userId: String, userPw: String, captchaCode: String?, importOption: Int)

    protected open suspend fun onDownloadCaptchaImage(httpClient: HttpClient, captchaUrl: String, importOption: Int): ByteArray =
        httpClient.get<HttpResponse>(captchaUrl).readBytes()
}