package tool.xfy9326.schedule.content.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.CallSuper
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import tool.xfy9326.schedule.content.beans.LoginPageInfo
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import java.io.Serializable

abstract class LoginCourseProvider<P : Serializable> : NetworkCourseProvider<P>() {
    private var loginPageInfo = LoginPageInfo.Empty

    open val enableCaptcha: Boolean = false

    suspend fun loadLoginPage(importOption: Int = 0) {
        loginPageInfo = onLoadLoginPage(importOption)
    }

    suspend fun login(userId: String, userPw: String, captchaCode: String?, importOption: Int = 0) =
        onLogin(userId, userPw, captchaCode, loginPageInfo, importOption)

    suspend fun getCaptchaImage(importOption: Int = 0) =
        try {
            val url = loginPageInfo.captchaUrl
            if (url == null) {
                null
            } else {
                onDownloadCaptchaImage(url, importOption)
            }
        } catch (e: Exception) {
            CourseAdapterException.Error.CAPTCHA_DOWNLOAD_ERROR.report(e)
        }


    protected abstract suspend fun onLoadLoginPage(importOption: Int): LoginPageInfo

    protected abstract suspend fun onLogin(userId: String, userPw: String, captchaCode: String?, loginPageInfo: LoginPageInfo, importOption: Int)

    protected open suspend fun onDownloadCaptchaImage(captchaUrl: String, importOption: Int): Bitmap? =
        httpClient.get(captchaUrl).bodyAsChannel().toInputStream().use(BitmapFactory::decodeStream)

    @CallSuper
    override suspend fun onClearConnection() {
        loginPageInfo = LoginPageInfo.Empty
    }
}