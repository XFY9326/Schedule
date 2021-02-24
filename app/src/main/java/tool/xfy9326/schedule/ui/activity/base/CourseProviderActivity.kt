package tool.xfy9326.schedule.ui.activity.base

import android.content.Context
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.base.CourseImportConfig
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.showShortToast
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.kt.tryCast
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.StrictImportModeWarningDialog
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel
import tool.xfy9326.schedule.utils.DialogUtils

abstract class CourseProviderActivity<I, P1 : AbstractCourseProvider<*>, P2 : AbstractCourseParser<*>, M : CourseProviderViewModel<I, P1, P2>, V : ViewBinding> :
    ViewModelActivity<M, V>(), ImportCourseConflictDialog.OnConfirmImportCourseConflictListener<Nothing> {
    companion object {
        const val EXTRA_COURSE_IMPORT_CONFIG = "EXTRA_COURSE_IMPORT_CONFIG"

        inline fun <reified T : CourseProviderActivity<*, *, *, *, *>> startProviderActivity(context: Context, config: CourseImportConfig<*, *, *, *>) {
            context.startActivity<T> { putExtra(EXTRA_COURSE_IMPORT_CONFIG, config) }
        }
    }

    // 应该在Override方法开始执行时CallSuper
    @CallSuper
    override fun onPrepare(viewBinding: V, viewModel: M) {
        viewModel.registerConfig(intent.getSerializableExtra(EXTRA_COURSE_IMPORT_CONFIG)?.tryCast()!!)
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
        viewModel.courseImportFinish.observeEvent(this, observer = ::onCourseImportFinish)
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

    override fun onConfirmImportCourseConflict(value: Nothing?) {
        finish()
    }

    @CallSuper
    protected open fun onCourseImportFinish(result: CourseProviderViewModel.ImportResult) {
        if (result.isSuccess) showShortToast(R.string.course_import_success)
        if (result.hasConflicts) ImportCourseConflictDialog.showDialog(supportFragmentManager)
    }

    protected abstract fun onShowCourseAdapterError(exception: CourseAdapterException)

    protected open fun onReadyImportCourse() {}

    class ImportRequestParams<I>(val isCurrentSchedule: Boolean, val importParams: I, val importOption: Int)
}