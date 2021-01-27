package tool.xfy9326.schedule.ui.activity

import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAppErrorBinding
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.vm.AppErrorViewModel

class AppErrorActivity : ViewModelActivity<AppErrorViewModel, ActivityAppErrorBinding>() {
    companion object {
        const val INTENT_EXTRA_CRASH_LOG = "CRASH_LOG"
    }

    override fun onBindLiveData(viewBinding: ActivityAppErrorBinding, viewModel: AppErrorViewModel) {
        viewModel.crashLog.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutAppError.showShortSnackBar(R.string.crash_detail_not_found)
            } else {
                CrashViewDialog.showDialog(supportFragmentManager, it)
            }
        }
        viewModel.updateInfo.observeEvent(this) {
            if (it.first) {
                val info = it.second
                if (info == null) {
                    viewBinding.layoutAppError.showShortSnackBar(R.string.no_new_update)
                } else {
                    UpgradeDialog.showDialog(supportFragmentManager, info)
                }
            } else {
                viewBinding.layoutAppError.showShortSnackBar(R.string.update_check_failed)
            }
        }
    }

    override fun onInitView(viewBinding: ActivityAppErrorBinding, viewModel: AppErrorViewModel) {
        setSupportActionBar(viewBinding.toolBarAppError.toolBarGeneral)

        val crashLogFileName = intent.getStringExtra(INTENT_EXTRA_CRASH_LOG)
        viewBinding.cardViewAppErrorCheckUpdate.setOnClickListener {
            viewModel.checkUpgrade()
        }
        viewBinding.cardViewAppErrorDetail.setOnClickListener {
            viewModel.loadCrashLogDetail(crashLogFileName)
        }
        viewBinding.cardViewAppErrorFeedback.setOnClickListener {
            startActivity<FeedbackActivity>()
        }
    }
}