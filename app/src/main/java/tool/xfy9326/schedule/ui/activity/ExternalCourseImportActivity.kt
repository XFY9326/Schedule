package tool.xfy9326.schedule.ui.activity

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import lib.xfy9326.android.kit.setOnSingleClickListener
import lib.xfy9326.kit.getDeepStackTraceString
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.getText
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.strictModeOnly
import tool.xfy9326.schedule.databinding.ActivityExternalCourseImportBinding
import tool.xfy9326.schedule.ui.activity.base.ViewBindingActivity
import tool.xfy9326.schedule.ui.activity.module.ScheduleLaunchModule
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleImportSuccessDialog
import tool.xfy9326.schedule.ui.dialog.StrictImportModeWarningDialog
import tool.xfy9326.schedule.ui.vm.ExternalCourseImportViewModel
import tool.xfy9326.schedule.utils.ExternalCourseImportUtils
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class ExternalCourseImportActivity : ViewBindingActivity<ActivityExternalCourseImportBinding>(), ImportCourseConflictDialog.OnReadImportCourseConflictListener {
    private lateinit var viewModel: ExternalCourseImportViewModel

    override fun onCreateViewBinding() = ActivityExternalCourseImportBinding.inflate(layoutInflater)

    override fun onValidateLaunch(savedInstanceState: Bundle?): Boolean {
        val viewModel = ExternalCourseImportUtils.prepareRunningEnvironment(this)
        return if (viewModel != null) {
            this.viewModel = viewModel
            true
        } else {
            false
        }
    }

    override fun onContentViewPreload(savedInstanceState: Bundle?) {
        installSplashScreen()
    }

    override fun onInitView(viewBinding: ActivityExternalCourseImportBinding) {
        setSupportActionBar(viewBinding.toolBarExternalCourse.toolBarGeneral)

        viewModel.providerError.observeEvent(this, javaClass.simpleName) {
            onCourseImportFailed()
            if (it.type.strictModeOnly) {
                StrictImportModeWarningDialog.showDialog(supportFragmentManager, it.getText(this), it.getDeepStackTraceString())
            } else {
                ViewUtils.showCourseAdapterErrorSnackBar(this, requireViewBinding().layoutExternalCourse, it)
            }
        }
        viewModel.courseImportFinish.observeEvent(this, javaClass.simpleName) {
            if (it.second != null) {
                if (it.first == ScheduleImportManager.ImportResult.SUCCESS_WITH_IGNORABLE_CONFLICTS) {
                    ImportCourseConflictDialog.showDialog(supportFragmentManager)
                } else if (it.first == ScheduleImportManager.ImportResult.SUCCESS) {
                    onCourseImportSuccess()
                }
            }
        }

        viewBinding.textViewExternalCourseSchoolName.apply {
            isSelected = true
            text = viewModel.schoolName
        }
        viewBinding.textViewExternalCourseSystemName.apply {
            isSelected = true
            text = viewModel.systemName
        }
        viewBinding.textViewExternalCourseAuthorName.apply {
            isSelected = true
            text = getString(R.string.adapter_author, viewModel.authorName)
        }
        viewBinding.buttonImportCourseToCurrentSchedule.setOnSingleClickListener {
            requestImportCourse(true)
        }
        viewBinding.buttonImportCourseToNewSchedule.setOnSingleClickListener {
            requestImportCourse(false)
        }
        viewBinding.buttonExternalCourseExit.setOnSingleClickListener {
            finish()
        }
        viewBinding.buttonExternalCourseOpenApp.setOnSingleClickListener {
            startActivity(IntentUtils.getLaunchAppIntent(this))
            finish()
        }
        viewBinding.textViewExternalCourseSuccessMsg.text = ScheduleImportSuccessDialog.getImportSuccessMsg(this, false)

        changeMainView(isInit = true, isLoading = false, isSuccess = false)

        ScheduleLaunchModule.tryShowEula(this)
    }

    override fun onReadImportCourseConflict(value: Bundle?) {
        onCourseImportSuccess()
    }

    override fun onBackPressed() {
        if (viewModel.isImportingCourses) {
            DialogUtils.showCancelScheduleImportDialog(this) {
                viewModel.finishImport()
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun requestImportCourse(isCurrentSchedule: Boolean) {
        if (isCurrentSchedule) {
            DialogUtils.showOverwriteScheduleAttentionDialog(this) {
                changeMainView(isInit = false, isLoading = true, isSuccess = false)
                viewModel.importCourse(true, null)
            }
        } else {
            DialogUtils.showNewScheduleNameDialog(this) {
                changeMainView(isInit = false, isLoading = true, isSuccess = false)
                viewModel.importCourse(false, it)
            }
        }
    }

    private fun onCourseImportSuccess() {
        changeMainView(isInit = false, isLoading = false, isSuccess = true)
        setResult(RESULT_OK)
    }

    private fun onCourseImportFailed() {
        changeMainView(isInit = true, isLoading = false, isSuccess = false)
    }

    private fun changeMainView(isInit: Boolean, isLoading: Boolean, isSuccess: Boolean) {
        requireViewBinding().layoutExternalCourseContent.isVisible = isInit
        requireViewBinding().layoutExternalCourseSuccess.isVisible = isSuccess
        if (isLoading) {
            requireViewBinding().progressBarExternalCourse.show()
        } else {
            requireViewBinding().progressBarExternalCourse.hide()
        }
    }
}