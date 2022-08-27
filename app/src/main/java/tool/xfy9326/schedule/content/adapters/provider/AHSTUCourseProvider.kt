package tool.xfy9326.schedule.content.adapters.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import io.github.xfy9326.atools.base.EMPTY
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.beans.LoginPageInfo
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import tool.xfy9326.schedule.content.utils.selectSingle

@Suppress("BlockingMethodInNonBlockingContext")
class AHSTUCourseProvider : LoginCourseProvider<Nothing>() {
    companion object {
        private const val JWXT_HOST = "jwxt.ahstu.edu.cn"
        private const val SSO_HOST = "sso.ahstu.edu.cn"
        private const val JWXT_URL = "http://$JWXT_HOST"
        private const val SSO_URL = "http://$SSO_HOST"

        @Suppress("SpellCheckingInspection")
        private const val SSO_LOGIN_URL = "$SSO_URL/sso/login?service=http://jwxt.ahstu.edu.cn/eams/login.action" // 登录地址
        private const val SSO_LOGOUT_URL = "$SSO_URL/sso/logout?service=http://jwxt.ahstu.edu.cn" // 退出地址
        private const val SSO_CHECK_CODE_URL = "$SSO_URL/sso/code/code.jsp" // 验证码地址
        private const val SSO_CHECK_CODE_VERIFY_URL = "$SSO_URL/sso/code/validationCode.jsp" // 验证 验证码是否正确
    }

    private val idsPattern = "addInput\\(form,\"ids\",\"(\\d+)\"\\)".toRegex()
    private val failIdsPattern = "value:\"(\\d+)\"".toRegex()

    private lateinit var cookieMap: Map<String, String>
    private val importOptionMap = ArrayList<Map<String, String>>()

    override val enableCaptcha = true
    private var getImportOptFail = false // 如果出错就调用教务系统里已经选择的学年学期

    override suspend fun onLoadLoginPage(importOption: Int): LoginPageInfo {
        cookieMap = Jsoup.connect(SSO_LOGIN_URL)
            .method(Connection.Method.GET)
            .execute()
            .cookies()
        return LoginPageInfo(SSO_CHECK_CODE_URL)
    }

    override suspend fun onLoadImportOptions(): Array<String> {
        val tpr = ArrayList<String>()
        try {
            val resTp = Jsoup.connect("http://app.xukela.top/data/pure_course_opt.json")
                .ignoreContentType(true)
                .execute()
                .body()
            val options = JSONArray(resTp)
            var i = 0
            while (i < options.length()) {
                val tpi: JSONObject = options.get(i) as JSONObject
                val tpMap = HashMap<String, String>()
                tpMap["id"] = tpi.get("id") as String
                tpMap["schoolYear"] = tpi.get("schoolYear") as String
                tpMap["name"] = tpi.get("name") as String
                importOptionMap.add(tpMap)
                tpr.add(tpMap["schoolYear"] + "学年 第" + tpMap["name"] + "学期")
                i += 1
            }
        } catch (e: Exception) {
            getImportOptFail = true
            CourseAdapterException.Error.IMPORT_OPTION_GET_ERROR.report()
        }
        return tpr.toTypedArray()
    }

    override suspend fun onDownloadCaptchaImage(captchaUrl: String, importOption: Int): Bitmap? {
        val codeStream = Jsoup.connect(captchaUrl)
            .cookies(cookieMap)
            .ignoreContentType(true)
            .execute()
            .bodyStream()

        return BitmapFactory.decodeStream(codeStream)
    }

    override suspend fun onLogin(userId: String, userPw: String, captchaCode: String?, loginPageInfo: LoginPageInfo, importOption: Int) {
        if (captchaCode == null || captchaCode.length != 4) CourseAdapterException.Error.CAPTCHA_CODE_ERROR.report() // 验证码错误

        val verifyCodeResTp: String = Jsoup.connect("$SSO_CHECK_CODE_VERIFY_URL?code=$captchaCode")
            .cookies(cookieMap)
            .execute()
            .body()
        val verifyCodeRes = JSONObject(verifyCodeResTp)
        if (!verifyCodeRes.getBoolean("success")) CourseAdapterException.Error.CAPTCHA_CODE_ERROR.report() // 验证码错误
        val passwordB64 = Base64.encodeToString(userPw.toByteArray(), Base64.NO_WRAP) // base64密码
        val loginRes = Jsoup.connect(SSO_LOGIN_URL)
            .method(Connection.Method.POST)
            .cookies(cookieMap)
            .data("username", userId)
            .data("password", passwordB64)
            .data("code", captchaCode)
            .data("lt", "e1s1")
            .data("_eventId", "submit")
            .followRedirects(false)
            .execute()

        if (!loginRes.hasHeader("Location")) CourseAdapterException.Error.LOGIN_SERVER_ERROR.report(msg = "登录失败了")
        val loginUrl = loginRes.header("Location") ?: EMPTY
        cookieMap = Jsoup.connect(loginUrl)
            .method(Connection.Method.GET)
            .followRedirects(false)
            .ignoreHttpErrors(true)
            .execute()
            .cookies()
    }

    private fun logout() {
        try {
            Jsoup.connect(SSO_LOGOUT_URL)
                .cookies(cookieMap)
                .execute()
        } catch (e: HttpStatusException) {
            // Ignore
        }
    }

    override suspend fun onLoadCoursesHtml(importOption: Int): String {
        val tp1 = Jsoup.connect("$JWXT_URL/eams/courseTableForStd.action")
            .method(Connection.Method.GET)
            .cookies(cookieMap)
            .execute().body()

        val idsMatcher = idsPattern.find(tp1)!!
        val ids = idsMatcher.groupValues[1]
        val semesterId: String = if (getImportOptFail) {
            failIdsPattern.find(tp1)!!.groupValues[1]
        } else {
            importOptionMap[importOption]["id"].toString()
        }
        val courseHtml: String = Jsoup.connect("$JWXT_URL/eams/courseTableForStd!courseTable.action")
            .method(Connection.Method.GET)
            .cookies(cookieMap)
            .data("setting.kind", "std")
            .data("semester.id", semesterId)
            .data("ids", ids)
            .execute()
            .body()

        // 从html中解析js
        val html = Jsoup.parse(courseHtml)
        val js = html.selectSingle("div#ExportA > script")

        return js.html()
    }

    override suspend fun onClearConnection() {
        logout()
        super.onClearConnection()
    }
}
