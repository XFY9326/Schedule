package tool.xfy9326.schedule.ui.activity.module

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.ui.showToast
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.ui.activity.AppErrorActivity
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractActivityModule
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.utils.UpgradeUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleDataProcessor
import tool.xfy9326.schedule.utils.view.DialogUtils
import kotlin.system.exitProcess

class ScheduleLaunchModule(activity: ScheduleActivity) : AbstractActivityModule<ScheduleActivity>(activity) {
    companion object {
        const val INTENT_EXTRA_CRASH_RELAUNCH = "CRASH_RELAUNCH"
        const val INTENT_EXTRA_APP_ERROR = "APP_ERROR"
        const val INTENT_EXTRA_APP_ERROR_CRASH_LOG = "APP_ERROR_CRASH_LOG"

        fun tryShowEula(activity: AppCompatActivity) {
            activity.lifecycleScope.launch {
                val currentEULAVersion = activity.resources.getInteger(R.integer.eula_version)
                if (!AppDataStore.hasAcceptedEULA()) {
                    if (AppDataStore.acceptEULAVersionFlow.first() < currentEULAVersion) {
                        showEULADialog(activity, true, currentEULAVersion)
                    }
                } else {
                    showEULADialog(activity, false, currentEULAVersion)
                }
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun showEULADialog(activity: AppCompatActivity, isUpdate: Boolean, newEULAVersion: Int) {
            DialogUtils.showEULADialog(activity, isUpdate) {
                if (it) {
                    GlobalScope.launch { AppDataStore.setAcceptEULAVersion(newEULAVersion) }
                } else {
                    activity.finishAndRemoveTask()
                }
            }
        }

        fun checkDoAppErrorLaunch(activity: AppCompatActivity): Boolean {
            val app = activity.applicationContext
            if (activity.intent.getBooleanExtra(INTENT_EXTRA_CRASH_RELAUNCH, false)) {
                try {
                    app.showToast(R.string.crash_relaunch_attention)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (activity.intent.getBooleanExtra(INTENT_EXTRA_APP_ERROR, false)) {
                try {
                    val crashLog = activity.intent.getStringExtra(INTENT_EXTRA_APP_ERROR_CRASH_LOG)
                    app.startActivity<AppErrorActivity> {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(AppErrorActivity.INTENT_EXTRA_CRASH_LOG, crashLog)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    exitProcess(1)
                }
                return false
            }
            return true
        }
    }

    private var isPreloadReady = false

    override fun onInit() {
        requireActivity().installSplashScreen().setKeepOnScreenCondition {
            !isPreloadReady
        }
        preloadData()
    }

    fun checkUpgrade() {
        if (isFirstLaunch) {
            UpgradeUtils.checkUpgrade(requireActivity(), false,
                onFoundUpgrade = { UpgradeDialog.showDialog(requireActivity().supportFragmentManager, it) }
            )
        }
    }

    private fun preloadData() {
        if (isFirstLaunch) {
            launch {
                ScheduleDataProcessor.preload()
                isPreloadReady = true
            }
        } else {
            isPreloadReady = true
        }
    }
}