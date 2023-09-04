package tool.xfy9326.schedule.ui.activity.module

import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.WeekDay.Companion.calWeekDay
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.dialog.DatePickerDialog
import tool.xfy9326.schedule.ui.dialog.NumberEditDialog
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel
import tool.xfy9326.schedule.utils.schedule.CourseTimeUtils
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        requireViewBinding().textViewScheduleStartDate.setOnSingleClickListener {
            requireViewModel().selectScheduleDate(true, requireViewModel().editSchedule.startDate)
        }
        requireViewBinding().buttonScheduleStartDateEdit.setOnSingleClickListener {
            requireViewModel().selectScheduleDate(true, requireViewModel().editSchedule.startDate)
        }

        requireViewBinding().textViewScheduleEndDate.setOnSingleClickListener {
            requireViewModel().selectScheduleDate(false, requireViewModel().editSchedule.endDate)
        }
        requireViewBinding().buttonScheduleEndDateEdit.setOnSingleClickListener {
            requireViewModel().selectScheduleDate(false, requireViewModel().editSchedule.endDate)
        }

        requireViewBinding().layoutScheduleWeekNum.setOnSingleClickListener {
            showMaxWeekNumEditDialog()
        }

        DatePickerDialog.setOnDateSetListener(requireActivity().supportFragmentManager, requireActivity()) { tag, date ->
            if (tag == TAG_SCHEDULE_START_DATE) {
                updateScheduleDate(true, date, true)
            } else if (tag == TAG_SCHEDULE_END_DATE) {
                updateScheduleDate(false, date, true)
            }
        }
        NumberEditDialog.setOnNumberChangedListener(requireActivity().supportFragmentManager, requireActivity()) { _, num ->
            val newTermEnd = requireViewModel().editSchedule.let {
                CourseTimeUtils.calculateTermEndDate(it.startDate, num, it.weekStart)
            }
            updateScheduleDate(false, newTermEnd, true)
        }
    }

    private fun showMaxWeekNumEditDialog() {
        requireActivity().apply {
            NumberEditDialog.showDialog(
                fragmentManager = supportFragmentManager,
                tag = null,
                number = getMaxWeekNum(),
                minNumber = 1,
                maxNumber = ScheduleUtils.MAX_WEEK_NUM,
                title = getString(R.string.schedule_week_num),
                editHint = getString(R.string.week_num_title)
            )
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