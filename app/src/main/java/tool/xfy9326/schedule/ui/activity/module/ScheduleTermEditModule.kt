package tool.xfy9326.schedule.ui.activity.module

import lib.xfy9326.android.kit.setOnSingleClickListener
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.WeekDay.Companion.calWeekDay
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.dialog.DatePickerDialog
import tool.xfy9326.schedule.ui.dialog.MaxWeekNumEditDialog
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import java.text.SimpleDateFormat
import java.util.*

class ScheduleTermEditModule(activity: ScheduleEditActivity) :
    AbstractViewModelActivityModule<ScheduleEditViewModel, ActivityScheduleEditBinding, ScheduleEditActivity>(activity) {
    companion object {
        private const val TAG_SCHEDULE_START_DATE = "SCHEDULE_START_DATE"
        private const val TAG_SCHEDULE_END_DATE = "SCHEDULE_END_DATE"
    }

    private val scheduleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onInit() {
        requireViewModel().selectScheduleDate.observeEvent(requireActivity()) {
            DatePickerDialog.showDialog(
                requireActivity().supportFragmentManager,
                if (it.first) TAG_SCHEDULE_START_DATE else TAG_SCHEDULE_END_DATE,
                it.second,
                it.third.calWeekDay
            )
        }

        requireViewBinding().buttonScheduleStartDateEdit.setOnSingleClickListener {
            requireViewModel().selectScheduleDate(true, requireViewModel().editSchedule.startDate)
        }
        requireViewBinding().buttonScheduleEndDateEdit.setOnSingleClickListener {
            requireViewModel().selectScheduleDate(false, requireViewModel().editSchedule.endDate)
        }
        requireViewBinding().layoutScheduleWeekNum.setOnSingleClickListener {
            MaxWeekNumEditDialog.showDialog(requireActivity().supportFragmentManager, getMaxWeekNum())
        }

        DatePickerDialog.setOnDateSetListener(requireActivity().supportFragmentManager, requireActivity()) { tag, date ->
            if (tag == TAG_SCHEDULE_START_DATE) {
                updateScheduleDate(true, date, true)
            } else if (tag == TAG_SCHEDULE_END_DATE) {
                updateScheduleDate(false, date, true)
            }
        }
        MaxWeekNumEditDialog.setOnWeekNumChangedListener(requireActivity().supportFragmentManager, requireActivity()) { num ->
            val newTermEnd = requireViewModel().editSchedule.let {
                CourseTimeUtils.calculateTermEndDate(it.startDate, num, it.weekStart)
            }
            updateScheduleDate(false, newTermEnd, true)
        }
    }

    fun updateScheduleDate(isStart: Boolean, date: Date, updateEditCache: Boolean) {
        val editSchedule = requireViewModel().editSchedule
        if (isStart) {
            if (updateEditCache) editSchedule.startDate = date
            requireViewBinding().textViewScheduleStartDate
        } else {
            if (updateEditCache) editSchedule.endDate = date
            requireViewBinding().textViewScheduleEndDate
        }.text = scheduleDateFormat.format(date)
        requireViewBinding().textViewScheduleWeekNum.text = requireActivity().getString(R.string.schedule_week_num_hint, getMaxWeekNum())
    }

    private fun getMaxWeekNum(): Int = requireViewModel().editSchedule.let {
        CourseTimeUtils.getMaxWeekNum(it.startDate, it.endDate, it.weekStart)
    }
}