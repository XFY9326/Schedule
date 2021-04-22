package tool.xfy9326.schedule.ui.activity

import androidx.core.view.isVisible
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAppErrorBinding
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.kt.startActivity
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
                viewBinding.layoutAppError.showShortSnackBar(R.string.crash_detail_not_found)
            } else {
                CrashViewDialog.showDialog(supportFragmentManager, it)
            }
        }
    }

    override fun onInitView(viewBinding: ActivityAppErrorBinding, viewModel: AppErrorViewModel) {
        setSupportActionBar(viewBinding.toolBarAppError.toolBarGeneral)

        val crashLogFileName = intent.getStringExtra(INTENT_EXTRA_CRASH_LOG)
        viewBinding.cardViewAppErrorCheckUpdate.setOnClickListener {
            UpgradeUtils.checkUpgrade(this, true,
                onFailed = { viewBinding.layoutAppError.showShortSnackBar(R.string.update_check_failed) },
                onNoUpgrade = { viewBinding.layoutAppError.showShortSnackBar(R.string.no_new_update) },
                onFoundUpgrade = { UpgradeDialog.showDialog(supportFragmentManager, it) }
            )
        }
        viewBinding.cardViewAppErrorDetail.setOnClickListener {
            viewModel.loadCrashLogDetail(crashLogFileName)
        }
        viewBinding.cardViewAppErrorFeedback.setOnClickListener {
            startActivity<FeedbackActivity>()
        }

        if (crashLogFileName != null) {
            viewBinding.cardViewAppErrorSend.setOnClickListener {
                IntentUtils.sendCrashReport(this, crashLogFileName)
            }
        } else {
            viewBinding.cardViewAppErrorSend.isVisible = false
        }
    }
}