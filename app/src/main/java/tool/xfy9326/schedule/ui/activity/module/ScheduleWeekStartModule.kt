package tool.xfy9326.schedule.ui.activity.module

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xfy9326.atools.ui.getStringArray
import io.github.xfy9326.atools.ui.show
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel

class ScheduleWeekStartModule(activity: ScheduleEditActivity) :
    AbstractViewModelActivityModule<ScheduleEditViewModel, ActivityScheduleEditBinding, ScheduleEditActivity>(activity) {
    override fun onInit() {
        requireViewBinding().layoutScheduleWeekStart.setOnClickListener {
            showWeekStartSelectDialog()
        }
    }

    private fun showWeekStartSelectDialog() {
        val selectedItem = when (requireViewModel().editSchedule.weekStart) {
            WeekDay.MONDAY -> 0
            WeekDay.SUNDAY -> 1
            else -> error("Unsupported week start day")
        }
        var selectedWeekDay = requireViewModel().editSchedule.weekStart
        MaterialAlertDialogBuilder(requireActivity()).apply {
            setTitle(R.string.schedule_week_start)
            setSingleChoiceItems(R.array.first_day_of_week, selectedItem) { _, which ->
                selectedWeekDay = when (which) {
                    0 -> WeekDay.MONDAY
                    1 -> WeekDay.SUNDAY
                    else -> error("Unsupported week start day")
                }
            }
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                requireViewModel().editSchedule.weekStart = selectedWeekDay
                updateWeekStartText(selectedWeekDay)
            }
        }.show(requireActivity())
    }

    fun updateWeekStartText(weekDay: WeekDay) {
        requireViewBinding().textViewScheduleWeekStart.text = requireActivity().getStringArray(R.array.first_day_of_week)[
            when (weekDay) {
                WeekDay.MONDAY -> 0
                WeekDay.SUNDAY -> 1
                else -> error("Unsupported week start day")
            }
        ]
    }
}