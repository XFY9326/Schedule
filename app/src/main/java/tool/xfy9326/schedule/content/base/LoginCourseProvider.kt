package tool.xfy9326.schedule.content.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.ktor.client.request.*
import tool.xfy9326.schedule.content.beans.LoginPageInfo
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import java.io.InputStream
import java.io.Serializable

abstract class LoginCourseProvider<P : Serializable> : NetworkCourseProvider<P>() {
    open val enableCaptcha: Boolean = false

    suspend fun loadLoginPage(importOption: Int = 0) = onLoadLoginPage(importOption)

    suspend fun login(userId: String, userPw: String, captchaCode: String?, loginPageInfo: LoginPageInfo, importOption: Int = 0) =
        onLogin(userId, userPw, captchaCode, loginPageInfo, importOption)

    suspend fun getCaptchaImage(captchaUrl: String, importOption: Int = 0) =
        try {
            onDownloadCaptchaImage(captchaUrl, importOption)
        } catch (e: Exception) {
            CourseAdapterException.Error.CAPTCHA_DOWNLOAD_ERROR.report(e)
        }


    protected abstract suspend fun onLoadLoginPage(importOption: Int): LoginPageInfo

    protected abstract suspend fun onLogin(userId: String, userPw: String, captchaCode: String?, loginPageInfo: LoginPageInfo, importOption: Int)

    protected open suspend fun onDownloadCaptchaImage(captchaUrl: String, importOption: Int): Bitmap? =
        httpClient.get<InputStream>(captchaUrl).use(BitmapFactory::decodeStream)
}