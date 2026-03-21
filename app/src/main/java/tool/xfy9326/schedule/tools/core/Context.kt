package tool.xfy9326.schedule.tools.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import tool.xfy9326.schedule.tools.core.startup.ToolsInitializer
import kotlin.system.exitProcess

val AppContext: Context
    get() = ToolsInitializer.applicationContext

val Context.packageUri: Uri
    get() = "package:${packageName}".toUri()

inline fun <reified A : Activity> Context.startActivity(intentBlock: Intent.() -> Unit = {}) {
    startActivity(Intent(this, A::class.java).apply(intentBlock))
}

fun Context.relaunchApplication(statusCode: Int = 0, intentBlock: Intent.() -> Unit = {}) {
    startActivity(packageManager.getLaunchIntentForPackage(packageName)!!.apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentBlock.invoke(this)
    })
    exitProcess(statusCode)
}

@SuppressLint("QueryPermissionsNeeded")
fun Context.tryStartActivity(intent: Intent, options: Bundle? = null): Boolean {
    // 由于兼容性问题，部分系统无法查询到Intent是否可以被处理
    val queryActivity = intent.resolveActivity(packageManager) != null
    return if (queryActivity) {
        startActivity(intent, options)
        true
    } else {
        runCatching {
            startActivity(intent, options)
        }.isSuccess
    }
}

fun Context.hideKeyboard(windowToken: IBinder) {
    getSystemService<InputMethodManager>()?.apply {
        if (isActive) hideSoftInputFromWindow(windowToken, 0)
    }
}
