package tool.xfy9326.schedule.ui.activity.module

import androidx.activity.result.contract.ActivityResultContracts
import io.github.xfy9326.atools.livedata.observeEvent
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.PermissionUtils
import tool.xfy9326.schedule.utils.showSnackBar
import tool.xfy9326.schedule.utils.view.DialogUtils

class CalendarSyncModule(activity: ScheduleActivity) :
    AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity) {
    private val requestCalendarPermission = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (PermissionUtils.checkGrantResults(it)) {
            requireViewModel().syncToCalendar()
        } else {
            requireViewBinding().layoutSchedule.showSnackBar(R.string.calendar_permission_get_failed)
        }
    }

    override fun onInit() {
        requireViewModel().syncToCalendarStatus.observeEvent(requireActivity()) {
            if (it.success) {
                if (it.failedAmount == 0) {
                    requireViewBinding().layoutSchedule.showSnackBar(R.string.calendar_sync_success)
                } else {
                    requireViewBinding().layoutSchedule.showSnackBar(R.string.calendar_sync_failed, it.total, it.failedAmount)
                }
            } else {
                requireViewBinding().layoutSchedule.showSnackBar(R.string.calendar_sync_error)
            }
        }
    }

    fun syncCalendar() {
        withShownCalendarSyncAttention {
            syncScheduleToCalendar()
        }
    }

    private fun withShownCalendarSyncAttention(block: () -> Unit) {
        launch {
            if (AppDataStore.hasShownCalendarSyncAttention()) {
                block()
            } else {
                DialogUtils.showCalendarSyncAttentionDialog(requireActivity()) {
                    block()
                }
            }
        }
    }

    private fun syncScheduleToCalendar() {
        launch {
            if (PermissionUtils.checkCalendarPermission(requireActivity(), requestCalendarPermission)) {
                requireViewModel().syncToCalendar()
            }
        }
    }
}