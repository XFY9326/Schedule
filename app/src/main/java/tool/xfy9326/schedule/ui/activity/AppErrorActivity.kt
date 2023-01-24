package tool.xfy9326.schedule.ui.activity

import androidx.core.view.isVisible
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAppErrorBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.vm.AppErrorViewModel
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.UpgradeUtils
import java.io.File

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
            IntentUtils.openFeedbackUrl(this)
        }

        val crashLogFilePath = intent?.getStringExtra(INTENT_EXTRA_CRASH_LOG)
        if (crashLogFilePath != null) {
            viewBinding.cardViewAppErrorDetail.setOnSingleClickListener {
                viewModel.loadCrashLogDetail(crashLogFilePath)
            }
            viewBinding.cardViewAppErrorSend.setOnSingleClickListener {
                IntentUtils.sendCrashReport(this, File(crashLogFilePath))
            }
        } else {
            viewBinding.cardViewAppErrorDetail.isVisible = false
            viewBinding.cardViewAppErrorSend.isVisible = false
        }
    }
}