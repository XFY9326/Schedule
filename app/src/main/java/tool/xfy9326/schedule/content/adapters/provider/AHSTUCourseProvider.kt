package tool.xfy9326.schedule.content.adapters.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import tool.xfy9326.schedule.content.base.LoginCourseProvider
import tool.xfy9326.schedule.content.beans.LoginPageInfo
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.report
import android.util.Base64
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AHSTUCourseProvider : LoginCourseProvider<Nothing>() {
    companion object {
        private var Cookies: Map<String, String> = HashMap()
        private const val JWXT_HOST = "jwxt.ahstu.edu.cn"
        private const val SSO_HOST = "sso.ahstu.edu.cn"
        private const val JWXT_URL = "http://$JWXT_HOST"
        private const val SSO_URL = "http://$SSO_HOST"

        @Suppress("SpellCheckingInspection")
        private const val SSO_LOGIN_URL = "$SSO_URL/sso/login?service=http://jwxt.ahstu.edu.cn/eams/login.action"//登录地址
        private const val SSO_LOGOUT_URL = "$SSO_URL/sso/logout?service=http://jwxt.ahstu.edu.cn"//退出地址
        private const val SSO_CHECK_CODE_URL = "$SSO_URL/sso/code/code.jsp"//验证码地址
        private const val SSO_CHECK_CODE_VERIFY_URL = "$SSO_URL/sso/code/validationCode.jsp"//验证 验证马是否正确
        private val ImportOptions = ArrayList<Map<String, String>>()

    }

    override val enableCaptcha = true
    private var getImportOptFail = false//如果出错就调用教务系统里已经选择的学年学期

    override suspend fun onLoadLoginPage(importOption: Int): LoginPageInfo {
        Cookies = Jsoup.connect(SSO_LOGIN_URL)
            .method(Connection.Method.GET)
            .execute()
            .cookies()
        return LoginPageInfo(SSO_CHECK_CODE_URL)
    }

    override suspend fun onLoadImportOptions(): Array<String> {
        val tpr = ArrayList<String>()
        try {
            val res_tp = Jsoup.connect("http://app.xukela.top/data/pure_course_opt.json")
                .ignoreContentType(true)
                .execute()
                .body()
            val Option = JSONArray(res_tp)
            var i = 0
            while (i < Option.length()) {
                val tpi: JSONObject = Option.get(i) as JSONObject
                val tpii = HashMap<String, String>()
                tpii["id"] = tpi.get("id") as String
                tpii["schoolYear"] = tpi.get("schoolYear") as String
                tpii["name"] = tpi.get("name") as String
                ImportOptions.add(tpii)
                tpr.add(tpii["schoolYear"] + "学年 第" + tpii["name"] + "学期")
                i += 1
            }
        } catch (e: Exception) {
            getImportOptFail = true
            CourseAdapterException.Error.IMPORT_OPTION_GET_ERROR.report()
        }
        return tpr.toTypedArray()
    }

    override suspend fun onDownloadCaptchaImage(captchaUrl: String, importOption: Int): Bitmap? {
        val code_bytes = Jsoup.connect(captchaUrl)
            .cookies(Cookies)
            .ignoreContentType(true)
            .execute().bodyAsBytes()

        return BitmapFactory.decodeByteArray(code_bytes, 0, code_bytes.size)
    }


    override suspend fun onLogin(userId: String, userPw: String, captchaCode: String?, loginPageInfo: LoginPageInfo, importOption: Int) {
        if (captchaCode != null && captchaCode.length != 4) CourseAdapterException.Error.CAPTCHA_CODE_ERROR.report()//验证码错误

        val verify_code_res_tp: String = Jsoup.connect("$SSO_CHECK_CODE_VERIFY_URL?code=$captchaCode")
            .cookies(Cookies)
            .execute()
            .body()
        val verify_code_res = JSONObject(verify_code_res_tp)
        if (!verify_code_res.getBoolean("success")) CourseAdapterException.Error.CAPTCHA_CODE_ERROR.report()//验证码错误
        val password_b64 = Base64.encodeToString(userPw.toByteArray(), Base64.NO_WRAP)//base64密码
        val login_res = Jsoup.connect(SSO_LOGIN_URL)
            .method(Connection.Method.POST)
            .cookies(Cookies)
            .data("username", userId)
            .data("password", password_b64)
            .data("code", captchaCode)
            .data("lt", "e1s1")
            .data("_eventId", "submit")
            .followRedirects(false)
            .execute()


        if (!login_res.hasHeader("Location")) CourseAdapterException.Error.LOGIN_SERVER_ERROR.report(msg = "登录失败了")
        val login_url = login_res.header("Location") ?: ""
        Cookies = Jsoup.connect(login_url)
            .method(Connection.Method.GET)
            .followRedirects(false)
            .ignoreHttpErrors(true)
            .execute()
            .cookies()

    }

    private suspend fun logout() {
        Jsoup.connect(SSO_LOGOUT_URL)
            .cookies(Cookies)
            .execute()
    }

    override suspend fun onLoadCoursesHtml(importOption: Int): String {
        val tp1 = Jsoup.connect("$JWXT_URL/eams/courseTableForStd.action")
            .method(Connection.Method.GET)
            .cookies(Cookies)
            .execute().body()
        val ids_pattern = Pattern.compile("addInput\\(form,\"ids\",\"(\\d+)\"\\)")
        val ids_matcher = ids_pattern.matcher(tp1)
        ids_matcher.find()
        val ids: String = ids_matcher.group(1)
        val semesterId: String
        if (getImportOptFail) {
            val failIds_matcher = Pattern.compile("value:\"(\\d+)\"").matcher(tp1)
            failIds_matcher.find()
            semesterId = failIds_matcher.group(1)
        } else {
            semesterId = ImportOptions[importOption]["id"].toString()
        }
        val course_html: String = Jsoup.connect("$JWXT_URL/eams/courseTableForStd!courseTable.action")
            .method(Connection.Method.GET)
            .cookies(Cookies)
            .data("setting.kind", "std")
            .data("semester.id", semesterId)
            .data("ids", ids)
            .execute()
            .body()

        //从html中解析js
        val html = Jsoup.parse(course_html)
        val js = html.select("div#ExportA > script")[0]

        return js.html()
    }


    override suspend fun onClearConnection() {
        logout()
        super.onClearConnection()
    }
}