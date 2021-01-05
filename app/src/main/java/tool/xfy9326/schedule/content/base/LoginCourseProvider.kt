@file:Suppress("unused")

package tool.xfy9326.schedule.content.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.ktor.client.*
import io.ktor.client.request.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import java.io.InputStream

/**
 * Login course provider
 *
 * @constructor Create empty Login course provider
 */
abstract class LoginCourseProvider : NetworkCourseProvider() {

    suspend fun loadCaptchaUrl(importOption: Int) = onLoadCaptcha(requireHttpClient(), importOption)

    suspend fun getCaptchaImage(url: String) = onDownloadCaptcha(requireHttpClient(), url)

    suspend fun login(userId: String, userPw: String, captchaCode: String?, importOption: Int) =
        onLogin(requireHttpClient(), userId, userPw, captchaCode, importOption)

    /**
     * Load captcha
     *
     * @param httpClient Ktor HttpClient
     * @param importOption Import option, Default: 0
     * @return Captcha image url
     */
    protected open suspend fun onLoadCaptcha(httpClient: HttpClient, importOption: Int): String? = null

    /**
     * Login
     *
     * @param httpClient Ktor HttpClient
     * @param userId User name
     * @param userPw User password
     * @param captchaCode Captcha code, Default: null
     * @param importOption Import option, Default: 0
     */
    protected abstract suspend fun onLogin(httpClient: HttpClient, userId: String, userPw: String, captchaCode: String?, importOption: Int)

    /**
     * Download captcha image
     *
     * @param httpClient Ktor HttpClient
     * @param url Captcha image url
     * @return Captcha image
     */
    protected open suspend fun onDownloadCaptcha(httpClient: HttpClient, url: String): Bitmap =
        try {
            BitmapFactory.decodeStream(httpClient.get<InputStream>(url))
        } catch (e: Exception) {
            CourseAdapterException.ErrorType.CAPTCHA_DOWNLOAD_ERROR.report(e)
        }
}