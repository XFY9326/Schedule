package tool.xfy9326.schedule.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.kt.showGlobalShortToast
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel

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
                preload()
                startMainActivity()
            }
        }
    }

    private fun preload() {
        runBlocking(Dispatchers.Default) {
            ScheduleViewModel.preload()
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