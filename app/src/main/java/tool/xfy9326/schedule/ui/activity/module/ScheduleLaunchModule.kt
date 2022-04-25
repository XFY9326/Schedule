package tool.xfy9326.schedule.ui.activity.module

import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import io.github.xfy9326.atools.coroutines.AppScope
import io.github.xfy9326.atools.ui.showToast
import io.github.xfy9326.atools.ui.startActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

        fun tryShowEula(activity: AppCompatActivity) {
            activity.lifecycleScope.launch {
                val currentEULAVersion = activity.resources.getInteger(R.integer.eula_version)
                if (AppDataStore.hasAcceptedEULA()) {
                    if (AppDataStore.acceptEULAVersionFlow.first() < currentEULAVersion) {
                        val eula = FileManager.readEULA()
                        DialogUtils.showEULAUpdateDialog(activity) {
                            if (it) {
                                showEULADialog(activity, eula, currentEULAVersion)
                            } else {
                                activity.finishAndRemoveTask()
                            }
                        }
                    }
                } else {
                    showEULADialog(activity, FileManager.readEULA(), currentEULAVersion)
                }
            }
        }

        private fun showEULADialog(activity: AppCompatActivity, eula: String, newEULAVersion: Int) {
            DialogUtils.showEULADialog(activity, eula, false) {
                if (it) {
                    AppScope.launch { AppDataStore.setAcceptEULAVersion(newEULAVersion) }
                } else {
                    activity.finishAndRemoveTask()
                }
            }
        }
    }

    private var isPreloadReady = false

    override fun onInit() {
        requireActivity().installSplashScreen().setKeepOnScreenCondition {
            !isPreloadReady
        }

        if (isFirstLaunch) {
            if (requireActivity().intent.getBooleanExtra(INTENT_EXTRA_APP_ERROR, false)) {
                startAppErrorActivity()
                isPreloadReady = true
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

    private fun startAppErrorActivity() {
        requireActivity().apply {
            startActivity<AppErrorActivity> {
                putExtra(AppErrorActivity.INTENT_EXTRA_CRASH_LOG, intent.getStringExtra(INTENT_EXTRA_APP_ERROR_CRASH_LOG))
            }
        }
    }
}