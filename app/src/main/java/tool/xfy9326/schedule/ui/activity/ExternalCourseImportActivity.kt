package tool.xfy9326.schedule.ui.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import io.github.xfy9326.atools.core.getLaunchAppIntent
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.beans.ExternalCourseImportData
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.strictModeOnly
import tool.xfy9326.schedule.databinding.ActivityExternalCourseImportBinding
import tool.xfy9326.schedule.kt.resume
import tool.xfy9326.schedule.ui.activity.base.ViewBindingActivity
import tool.xfy9326.schedule.ui.activity.module.ScheduleLaunchModule
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleImportSuccessDialog
import tool.xfy9326.schedule.ui.dialog.StrictImportModeWarningDialog
import tool.xfy9326.schedule.ui.vm.ExternalCourseImportViewModel
import tool.xfy9326.schedule.utils.getDeepStackTraceString
import tool.xfy9326.schedule.utils.schedule.ExternalCourseImportUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class ExternalCourseImportActivity : ViewBindingActivity<ActivityExternalCourseImportBinding>() {
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
        onBackPressedDispatcher.addCallback(this, true, this::onBackPressed)
    }

    override fun onInitView(viewBinding: ActivityExternalCourseImportBinding) {
        setSupportActionBar(viewBinding.toolBarExternalCourse.toolBarGeneral)

        viewModel.providerError.observeEvent(this, javaClass.simpleName) {
            onCourseImportFailed()
            if (it.type.strictModeOnly) {
                StrictImportModeWarningDialog.showDialog(supportFragmentManager, it.getText(this), it.getDeepStackTraceString())
            } else {
                ViewUtils.showCourseImportErrorSnackBar(this, requireViewBinding().layoutExternalCourse, it)
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
        ImportCourseConflictDialog.setOnReadImportCourseConflictListener(supportFragmentManager, this) {
            onCourseImportSuccess()
        }

        setupText(viewBinding)

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
            startActivity(getLaunchAppIntent()!!)
            finish()
        }

        changeMainView(isInit = viewModel.isInit, isLoading = viewModel.isLoading, isSuccess = viewModel.isSuccess)

        ScheduleLaunchModule.tryShowEula(this)
    }

    private fun setupText(viewBinding: ActivityExternalCourseImportBinding) {
        when (viewModel.importParams) {
            is ExternalCourseImportData.Origin.External -> {
                val adapterInfo = viewModel.adapterInfo
                viewBinding.textViewExternalCourseSchoolName.apply {
                    isSelected = true
                    text = adapterInfo.schoolName
                }
                viewBinding.textViewExternalCourseSystemName.apply {
                    isSelected = true
                    text = adapterInfo.systemName
                }
                viewBinding.textViewExternalCourseAuthorName.apply {
                    isSelected = true
                    text = getString(R.string.adapter_author, adapterInfo.authorName)
                    isVisible = true
                }
            }
            is ExternalCourseImportData.Origin.JSON -> {
                viewBinding.textViewExternalCourseSchoolName.apply {
                    isSelected = true
                    setText(R.string.json_course_import_title)
                }
                viewBinding.textViewExternalCourseSystemName.apply {
                    isSelected = true
                    setText(R.string.json_course_import_msg)
                }
                viewBinding.textViewExternalCourseAuthorName.isVisible = false
            }
        }
        viewBinding.textViewExternalCourseSuccessMsg.text = ScheduleImportSuccessDialog.getImportSuccessMsg(this, false)
    }

    private fun onBackPressed(callback: OnBackPressedCallback) {
        if (viewModel.isImportingCourses) {
            DialogUtils.showCancelScheduleImportDialog(this) {
                viewModel.finishImport()
                callback.resume(onBackPressedDispatcher)
            }
        } else {
            callback.resume(onBackPressedDispatcher)
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
        viewModel.isInit = isInit
        viewModel.isLoading = isLoading
        viewModel.isSuccess = isSuccess

        requireViewBinding().layoutExternalCourseContent.isVisible = isInit
        requireViewBinding().layoutExternalCourseSuccess.isVisible = isSuccess
        if (isLoading) {
            requireViewBinding().progressBarExternalCourse.show()
        } else {
            requireViewBinding().progressBarExternalCourse.hide()
        }
    }
}