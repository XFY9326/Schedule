package tool.xfy9326.schedule.ui.activity.base

import android.content.Context
import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.livedata.observeEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.beans.ScheduleImportRequestParams
import tool.xfy9326.schedule.content.base.AbstractCourseParser
import tool.xfy9326.schedule.content.base.AbstractCourseProvider
import tool.xfy9326.schedule.content.utils.BaseCourseImportConfig
import tool.xfy9326.schedule.content.utils.CourseAdapterException
import tool.xfy9326.schedule.content.utils.CourseAdapterException.Companion.strictModeOnly
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.kt.getSerializableExtraCompat
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.dialog.ImportCourseConflictDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleImportSuccessDialog
import tool.xfy9326.schedule.ui.dialog.StrictImportModeWarningDialog
import tool.xfy9326.schedule.ui.vm.base.CourseProviderViewModel
import tool.xfy9326.schedule.utils.getDeepStackTraceString
import tool.xfy9326.schedule.utils.schedule.ScheduleImportManager
import tool.xfy9326.schedule.utils.view.DialogUtils

abstract class CourseProviderActivity<I, P1 : AbstractCourseProvider<*>, P2 : AbstractCourseParser<*>, M : CourseProviderViewModel<I, P1, P2>, V : ViewBinding> :
    ViewModelActivity<M, V>() {
    companion object {
        private const val EXTRA_EDIT_SCHEDULE_ID = "EXTRA_EDIT_SCHEDULE_ID"

        const val EXTRA_COURSE_IMPORT_CONFIG_CLASS = "EXTRA_COURSE_IMPORT_CONFIG_CLASS"

        inline fun <reified T : CourseProviderActivity<*, *, *, *, *>> startProviderActivity(context: Context, config: BaseCourseImportConfig) {
            context.startActivity<T> {
                putExtra(EXTRA_COURSE_IMPORT_CONFIG_CLASS, config)
            }
        }
    }

    protected abstract val exitIfImportSuccess: Boolean

    // 应该在Override方法开始执行时CallSuper
    @CallSuper
    override fun onPrepare(viewBinding: V, viewModel: M) {
        viewModel.registerConfig(intent.getSerializableExtraCompat(EXTRA_COURSE_IMPORT_CONFIG_CLASS)!!)
    }

    @CallSuper
    override fun onBindLiveData(viewBinding: V, viewModel: M) {
        viewModel.providerError.observeEvent(this, javaClass.simpleName) {
            if (it.type.strictModeOnly) {
                StrictImportModeWarningDialog.showDialog(supportFragmentManager, it.getText(this), it.getDeepStackTraceString())
            } else {
                onShowCourseAdapterError(it)
            }
        }
        viewModel.courseImportFinish.observeEvent(this, CourseProviderActivity::class.simpleName!!) {
            onCourseImportFinish(it.first, it.second)
            internalCourseImportFinish(it.first, it.second)
        }
    }

    @CallSuper
    override fun onInitView(viewBinding: V, viewModel: M) {
        ScheduleImportSuccessDialog.setOnScheduleImportSuccessListener(supportFragmentManager, this) {
            if (it != null) {
                lifecycleScope.launch {
                    val isCurrentSchedule = AppDataStore.currentScheduleIdFlow.first() == it
                    ScheduleEditActivity.startActivity(this@CourseProviderActivity, it, isCurrentSchedule)
                    if (exitIfImportSuccess) finish()
                }
            } else {
                if (exitIfImportSuccess) finish()
            }
        }
        ImportCourseConflictDialog.setOnReadImportCourseConflictListener(supportFragmentManager, this) {
            it?.getLong(EXTRA_EDIT_SCHEDULE_ID)?.let { id ->
                ScheduleImportSuccessDialog.showDialog(supportFragmentManager, id)
            }
        }
    }

    protected fun requestImportCourse(params: ScheduleImportRequestParams<I>) {
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

    protected open fun onCourseImportFinish(result: ScheduleImportManager.ImportResult, editScheduleId: Long?) {}

    private fun internalCourseImportFinish(result: ScheduleImportManager.ImportResult, editScheduleId: Long?) {
        if (editScheduleId != null) {
            if (result == ScheduleImportManager.ImportResult.SUCCESS_WITH_IGNORABLE_CONFLICTS) {
                ImportCourseConflictDialog.showDialog(
                    supportFragmentManager, bundleOf(
                        EXTRA_EDIT_SCHEDULE_ID to editScheduleId
                    )
                )
            } else if (result == ScheduleImportManager.ImportResult.SUCCESS) {
                ScheduleImportSuccessDialog.showDialog(supportFragmentManager, editScheduleId)
            }
        }
    }

    protected abstract fun onShowCourseAdapterError(exception: CourseAdapterException)

    protected open fun onReadyImportCourse() {}

}