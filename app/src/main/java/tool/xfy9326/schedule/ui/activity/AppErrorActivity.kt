package tool.xfy9326.schedule.ui.activity

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityAppErrorBinding
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.dialog.CrashViewDialog
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.vm.AppErrorViewModel
import tool.xfy9326.schedule.utils.BackupUtils
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.UpgradeUtils
import tool.xfy9326.schedule.utils.consumeSystemBarInsets
import tool.xfy9326.schedule.utils.schedule.ScheduleBackupHelper
import tool.xfy9326.schedule.utils.showSnackBar
import java.io.File

class AppErrorActivity : ViewModelActivity<AppErrorViewModel, ActivityAppErrorBinding>() {
    companion object {
        const val INTENT_EXTRA_CRASH_LOG = "CRASH_LOG"
    }

    private val backupSchedule = registerForActivityResult(ActivityResultContracts.CreateDocument(MIMEConst.MIME_APPLICATION_JSON)) {
        requireViewModel().scheduleBackup.backupToUri(it)
    }

    override val vmClass = AppErrorViewModel::class
    override fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: AppErrorViewModel) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

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
        viewBinding.toolBarAppError.toolBarGeneral.consumeSystemBarInsets()
        viewBinding.layoutAppErrContent.consumeSystemBarInsets(bottom = true)

        viewModel.scheduleBackup.setupBackupView(
            context = this,
            lifecycleOwner = this,
            fragmentManager = supportFragmentManager,
            onBackupLaunch = {
                backupSchedule.launch(BackupUtils.createBackupFileName(this))
            },
            onBackupStatus = {
                viewBinding.layoutAppError.showSnackBar(ScheduleBackupHelper.getResultMsgId(it))
            }
        )
        viewBinding.cardViewAppErrorCheckUpdate.setOnSingleClickListener {
            UpgradeUtils.checkUpgrade(this, true,
                onFailed = { viewBinding.layoutAppError.showSnackBar(R.string.update_check_failed) },
                onNoUpgrade = { viewBinding.layoutAppError.showSnackBar(R.string.no_new_update) },
                onFoundUpgrade = { UpgradeDialog.showDialog(supportFragmentManager, it) }
            )
        }
        viewBinding.cardViewAppErrorBackup.setOnSingleClickListener {
            viewModel.scheduleBackup.requestBackupScheduleList()
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
            viewBinding.cardViewAppErrorDetail.setCardBackgroundColor(getColorCompat(android.R.color.darker_gray))
            viewBinding.cardViewAppErrorSend.setCardBackgroundColor(getColorCompat(android.R.color.darker_gray))
            viewBinding.cardViewAppErrorDetail.isEnabled = false
            viewBinding.cardViewAppErrorSend.isEnabled = false
        }
    }
}