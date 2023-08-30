package tool.xfy9326.schedule.ui.activity.module

import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.adapter.ScheduleTimeAdapter
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel
import tool.xfy9326.schedule.utils.showSnackBar
import tool.xfy9326.schedule.utils.view.DialogUtils

class ScheduleTimeImportModule(activity: ScheduleEditActivity) :
    AbstractViewModelActivityModule<ScheduleEditViewModel, ActivityScheduleEditBinding, ScheduleEditActivity>(activity) {
    private lateinit var scheduleTimeAdapter: ScheduleTimeAdapter

    fun bindScheduleTimeAdapter(scheduleTimeAdapter: ScheduleTimeAdapter) {
        this.scheduleTimeAdapter = scheduleTimeAdapter
    }

    override fun onInit() {
        requireViewBinding().layoutScheduleTimeEdit.buttonScheduleTimeImport.setOnSingleClickListener {
            requireViewModel().loadAllSchedules()
        }
        requireViewModel().loadAllSchedules.observeEvent(requireActivity()) {
            if (it.isEmpty()) {
                requireViewBinding().layoutScheduleEdit.showSnackBar(R.string.empty_schedule_list)
            } else {
                DialogUtils.showScheduleSelectDialog(requireActivity(), R.string.import_times_from_other_schedule, it) { _, id ->
                    requireViewModel().importScheduleTimes(id)
                }
            }
        }
        requireViewModel().importScheduleTimes.observeEvent(requireActivity()) {
            if (it == null) {
                requireViewBinding().layoutScheduleEdit.showSnackBar(R.string.schedule_time_import_failed)
            } else {
                requireViewModel().editSchedule.times = it
                scheduleTimeAdapter.submitList(it.toList())
                requireViewBinding().layoutScheduleEdit.showSnackBar(R.string.schedule_time_import_success)
            }
        }
    }
}