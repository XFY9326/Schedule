package tool.xfy9326.schedule.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.io.TextIO
import tool.xfy9326.schedule.kt.showGlobalShortToast
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.utils.DialogUtils
import tool.xfy9326.schedule.utils.DirUtils

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
            else -> standardLaunch()
        }
    }

    private fun validateFirstLaunch() =
        intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0 || intent.getBooleanExtra(INTENT_EXTRA_APP_RELAUNCH, false)

    private fun validateAppError() =
        intent.getBooleanExtra(INTENT_EXTRA_APP_ERROR, false)

    private fun validateCrashRelaunch() =
        intent.getBooleanExtra(INTENT_EXTRA_CRASH_RELAUNCH, false)

    private fun standardLaunch() {
        if (validateCrashRelaunch()) showGlobalShortToast(R.string.crash_relaunch_attention)
        lifecycleScope.launch {
            if (AppDataStore.acceptEULAFlow.first()) {
                startMainActivity()
            } else {
                showEULA(TextIO.readAssetText(DirUtils.ASSETS_EULA_FILE)!!)
            }
        }
    }

    private fun showEULA(eula: String) {
        DialogUtils.showEULADialog(this@SplashActivity, eula, false) {
            if (it) {
                runBlocking {
                    AppDataStore.setAcceptEULA(true)
                    startMainActivity()
                }
            } else {
                finishAndRemoveTask()
            }
        }
    }

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