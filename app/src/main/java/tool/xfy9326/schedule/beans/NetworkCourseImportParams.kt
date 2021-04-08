package tool.xfy9326.schedule.beans

import tool.xfy9326.schedule.content.beans.LoginPageInfo

class NetworkCourseImportParams(val userId: String, val userPw: String, val loginPageInfo: LoginPageInfo = LoginPageInfo.Empty, val captchaCode: String? = null)