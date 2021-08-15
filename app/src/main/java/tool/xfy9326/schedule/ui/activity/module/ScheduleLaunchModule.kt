package tool.xfy9326.schedule.ui.activity.module

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lib.xfy9326.android.kit.showToast
import lib.xfy9326.android.kit.startActivity
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.io.FileManager
import tool.xfy9326.schedule.ui.activity.AppErrorActivity
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractActivityModule
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.utils.UpgradeUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleDataProcessor
import tool.xfy9326.schedule.utils.view.DialogUtils

class ScheduleLaunchModule(activity: ScheduleActivity) : AbstractActivityModule<ScheduleActivity>(activity) {
    companion object {
        const val INTENT_EXTRA_CRASH_RELAUNCH = "CRASH_RELAUNCH"
        const val INTENT_EXTRA_APP_ERROR = "APP_ERROR"
        const val INTENT_EXTRA_APP_ERROR_CRASH_LOG = "APP_ERROR_CRASH_LOG"
    }

    private var isPreloadReady = false

    override fun onInit() {
        val splashScreen = requireActivity().installSplashScreen()
        splashScreen.setKeepVisibleCondition {
            !isPreloadReady
        }

        if (isFirstLaunch) {
            if (requireActivity().intent.getBooleanExtra(INTENT_EXTRA_APP_ERROR, false)) {
                startAppErrorActivity()
                requireActivity().finish()
                return
            }

            if (requireActivity().intent.getBooleanExtra(INTENT_EXTRA_CRASH_RELAUNCH, false)) {
                requireActivity().showToast(R.string.crash_relaunch_attention)
            }
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

    fun showEula() {
        launch {
            if (!AppDataStore.acceptEULAFlow.first()) {
                val eula = FileManager.readEULA()
                DialogUtils.showEULADialog(requireActivity(), eula, false) {
                    if (it) {
                        launch { AppDataStore.setAcceptEULA(true) }
                    } else {
                        requireActivity().finishAndRemoveTask()
                    }
                }
            }
        }
    }

    private fun startAppErrorActivity() {
        requireActivity().apply {
            startActivity<AppErrorActivity> {
                putExtra(AppErrorActivity.INTENT_EXTRA_CRASH_LOG, intent.getStringExtra(INTENT_EXTRA_APP_ERROR_CRASH_LOG))
            }
        }
    }
}