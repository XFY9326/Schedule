@file:Suppress("unused")

package tool.xfy9326.schedule.content.beans

import android.os.Bundle
import io.ktor.http.*

class LoginPageInfo(
    val captchaUrl: String? = null,
    val loginParams: Parameters? = null,
    val bundle: Bundle? = null,
) {
    companion object {
        val Empty = LoginPageInfo()
    }
}