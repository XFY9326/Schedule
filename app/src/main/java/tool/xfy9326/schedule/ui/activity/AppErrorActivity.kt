package tool.xfy9326.schedule.ui.activity

import androidx.core.view.isVisible
import lib.xfy9326.android.kit.setOnSingleClickListener
import lib.xfy9326.android.kit.startActivity
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAppErrorBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.vm.AppErrorViewModel
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.UpgradeUtils

class AppErrorActivity : ViewModelActivity<AppErrorViewModel, ActivityAppErrorBinding>() {
    companion object {
        const val INTENT_EXTRA_CRASH_LOG = "CRASH_LOG"
    }

    override val vmClass = AppErrorViewModel::class

    override fun onCreateViewBinding() = ActivityAppErrorBinding.inflate(layoutInflater)

    override fun onBindLiveData(viewBinding: ActivityAppErrorBinding, viewModel: AppErrorViewModel) {
        viewModel.crashLog.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutAppError.showSnackBar(R.string.crash_detail_not_found)
            } else {
                CrashViewDialog.showDialog(supportFragmentManager, it)
            }
        }
    }

    override fun onInitView(viewBinding: ActivityAppErrorBinding, viewModel: AppErrorViewModel) {
        setSupportActionBar(viewBinding.toolBarAppError.toolBarGeneral)

        viewBinding.cardViewAppErrorCheckUpdate.setOnClickListener {
            UpgradeUtils.checkUpgrade(this, true,
                onFailed = { viewBinding.layoutAppError.showSnackBar(R.string.update_check_failed) },
                onNoUpgrade = { viewBinding.layoutAppError.showSnackBar(R.string.no_new_update) },
                onFoundUpgrade = { UpgradeDialog.showDialog(supportFragmentManager, it) }
            )
        }
        viewBinding.cardViewAppErrorFeedback.setOnSingleClickListener {
            startActivity<FeedbackActivity>()
        }

        val crashLogFileName = intent?.getStringExtra(INTENT_EXTRA_CRASH_LOG)
        if (crashLogFileName != null) {
            viewBinding.cardViewAppErrorDetail.setOnSingleClickListener {
                viewModel.loadCrashLogDetail(crashLogFileName)
            }
            viewBinding.cardViewAppErrorSend.setOnSingleClickListener {
                IntentUtils.sendCrashReport(this, crashLogFileName)
            }
        } else {
            viewBinding.cardViewAppErrorDetail.isVisible = false
            viewBinding.cardViewAppErrorSend.isVisible = false
        }
    }
}