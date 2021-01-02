package tool.xfy9326.schedule.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.showGlobalShortToast
import tool.xfy9326.schedule.kt.startActivity

class SplashActivity : AppCompatActivity() {
    companion object {
        const val INTENT_EXTRA_CRASH_RELAUNCH = "CRASH_RELAUNCH"
        const val INTENT_EXTRA_APP_ERROR = "APP_ERROR"
        const val INTENT_EXTRA_APP_ERROR_CRASH_LOG = "APP_ERROR_CRASH_LOG"
        const val INTENT_EXTRA_APP_RELAUNCH = "APP_RELAUNCH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            validateFirstLaunch() -> finish()
            validateAppError() -> startAppErrorActivity()
            else -> {
                if (validateCrashRelaunch()) showGlobalShortToast(R.string.crash_relaunch_attention)
                startMainActivity()
            }
        }
    }

    private fun validateFirstLaunch() =
        intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0 || intent.getBooleanExtra(INTENT_EXTRA_APP_RELAUNCH, false)

    private fun validateAppError() =
        intent.getBooleanExtra(INTENT_EXTRA_APP_ERROR, false)

    private fun validateCrashRelaunch() =
        intent.getBooleanExtra(INTENT_EXTRA_CRASH_RELAUNCH, false)

    private fun startMainActivity() {
        startActivity<ScheduleActivity>()
        finishWithTransitionAnim()
    }

    private fun startAppErrorActivity() {
        startActivity<AppErrorActivity> {
            putExtra(AppErrorActivity.INTENT_EXTRA_CRASH_LOG, intent.getStringExtra(INTENT_EXTRA_APP_ERROR_CRASH_LOG))
        }
        finishWithTransitionAnim()
    }

    private fun finishWithTransitionAnim() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finishAfterTransition()
    }

    override fun onBackPressed() {}
}