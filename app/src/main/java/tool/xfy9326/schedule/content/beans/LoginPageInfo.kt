@file:Suppress("unused")

package tool.xfy9326.schedule.content.beans

import android.os.Bundle
import java.lang.reflect.Parameter

class LoginPageInfo(
    val captchaUrl: String? = null,
    val loginParams: Parameter? = null,
    val bundle: Bundle? = null,
) {
    companion object {
        val Empty = LoginPageInfo()
    }
}