package tool.xfy9326.schedule.ui.activity.base

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.content.base.AbstractCourseImportConfig
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.kt.castNonNull
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.tools.livedata.observeEvent
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleImportSuccessDialog
import tool.xfy9326.schedule.ui.dialog.StrictImportModeWarningDialog
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel
import tool.xfy9326.schedule.utils.view.DialogUtils

abstract class CourseProviderActivity<I, P1 : AbstractCourseProvider<*>, P2 : AbstractCourseParser<*>, M : CourseProviderViewModel<I, P1, P2>, V : ViewBinding> :
    ViewModelActivity<M, V>(), ImportCourseConflictDialog.OnReadImportCourseConflictListener,
    ScheduleImportSuccessDialog.OnScheduleImportSuccessListener {
    companion object {
        private const val EXTRA_EDIT_SCHEDULE_ID = "EXTRA_EDIT_SCHEDULE_ID"

        const val EXTRA_COURSE_IMPORT_CONFIG_CLASS = "EXTRA_COURSE_IMPORT_CONFIG_CLASS"

        inline fun <reified T : CourseProviderActivity<*, *, *, *, *>> startProviderActivity(context: Context, config: AbstractCourseImportConfig<*, *, *, *>) {
            context.startActivity<T> {
                putExtra(EXTRA_COURSE_IMPORT_CONFIG_CLASS, config)
            }
        }
    }

    protected abstract val exitIfImportSuccess: Boolean

    // 应该在Override方法开始执行时CallSuper
    @CallSuper
    override fun onPrepare(viewBinding: V, viewModel: M) {
        viewModel.registerConfig(intent.getSerializableExtra(EXTRA_COURSE_IMPORT_CONFIG_CLASS).castNonNull())
    }

    @CallSuper
    override fun onBindLiveData(viewBinding: V, viewModel: M) {
        viewModel.providerError.observeEvent(this) {
            if (it.type.strictMode) {
                StrictImportModeWarningDialog.showDialog(supportFragmentManager, it.getText(this), it.getDeepStackTraceString())
            } else {
                onShowCourseAdapterError(it)
            }
        }
        viewModel.courseImportFinish.observeEvent(this) {
            onCourseImportFinish(it.first, it.second)
            internalCourseImportFinish(it.first, it.second)
        }
    }

    protected fun requestImportCourse(params: ImportRequestParams<I>) {
        if (params.isCurrentSchedule) {
            DialogUtils.showOverwriteScheduleAttentionDialog(this) {
                onReadyImportCourse()
                requireViewModel().importCourse(params.importParams, params.importOption, true, null)
            }
        } else {
            DialogUtils.showNewScheduleNameDialog(this) {
                onReadyImportCourse()
                requireViewModel().importCourse(params.importParams, params.importOption, false, it)
            }
        }
    }

    protected open fun onCourseImportFinish(result: CourseProviderViewModel.ImportResult, editScheduleId: Long?) {}

    private fun internalCourseImportFinish(result: CourseProviderViewModel.ImportResult, editScheduleId: Long?) {
        if (editScheduleId != null) {
            if (result == CourseProviderViewModel.ImportResult.SUCCESS_WITH_IGNORABLE_CONFLICTS) {
                ImportCourseConflictDialog.showDialog(supportFragmentManager, bundleOf(
                    EXTRA_EDIT_SCHEDULE_ID to editScheduleId
                ))
            } else if (result == CourseProviderViewModel.ImportResult.SUCCESS) {
                ScheduleImportSuccessDialog.showDialog(supportFragmentManager, editScheduleId)
            }
        }
    }

    final override fun onReadImportCourseConflict(value: Bundle?) {
        value?.getLong(EXTRA_EDIT_SCHEDULE_ID)?.let {
            ScheduleImportSuccessDialog.showDialog(supportFragmentManager, it)
        }
    }

    final override fun onEditScheduleNow(scheduleId: Long) {
        startActivity<ScheduleEditActivity> {
            putExtra(ScheduleEditActivity.INTENT_EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(ScheduleEditActivity.INTENT_EXTRA_IS_CURRENT_SCHEDULE, false)
        }
        if (exitIfImportSuccess) finish()
    }

    final override fun onEditScheduleLater() {
        if (exitIfImportSuccess) finish()
    }

    protected abstract fun onShowCourseAdapterError(exception: CourseAdapterException)

    protected open fun onReadyImportCourse() {}

    class ImportRequestParams<I>(val isCurrentSchedule: Boolean, val importParams: I, val importOption: Int)
}