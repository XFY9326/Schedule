@file:Suppress("unused")

package tool.xfy9326.schedule.content.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.ktor.client.*
import io.ktor.client.request.*
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import java.io.InputStream
import java.io.Serializable

/**
 * Login course provider
 *
 * @constructor Create empty Login course provider
 */
abstract class LoginCourseProvider<P : Serializable>(params: P?) : NetworkCourseProvider<P>(params) {

    suspend fun getCaptchaImage(importOption: Int = 0) = onLoadCaptcha(requireHttpClient(), importOption)?.let {
        onGetCaptchaImage(requireHttpClient(), it)
    }

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
     * Read captcha image bytes
     *
     * @param httpClient Ktor HttpClient
     * @param url Captcha image url
     * @return Captcha image stream
     */
    protected open suspend fun onGetCaptchaImage(httpClient: HttpClient, url: String): Bitmap =
        try {
            httpClient.get<InputStream>(url).use(BitmapFactory::decodeStream)
        } catch (e: Exception) {
            CourseAdapterException.ErrorType.CAPTCHA_DOWNLOAD_ERROR.report(e)
        }
}