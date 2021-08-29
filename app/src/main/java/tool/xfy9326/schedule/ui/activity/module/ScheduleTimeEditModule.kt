package tool.xfy9326.schedule.ui.activity.module

import android.annotation.SuppressLint
import lib.xfy9326.android.kit.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.adapter.ScheduleTimeAdapter
import tool.xfy9326.schedule.ui.dialog.TimePickerDialog
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel

class ScheduleTimeEditModule(activity: ScheduleEditActivity) :
    AbstractViewModelActivityModule<ScheduleEditViewModel, ActivityScheduleEditBinding, ScheduleEditActivity>(activity) {
    companion object {
        private const val TAG_SCHEDULE_TIME_START_PREFIX = "TAG_SCHEDULE_TIME_START_"
        private const val TAG_SCHEDULE_TIME_END_PREFIX = "TAG_SCHEDULE_TIME_END_"
    }

    private lateinit var scheduleTimeAdapter: ScheduleTimeAdapter

    fun bindScheduleTimeAdapter(scheduleTimeAdapter: ScheduleTimeAdapter) {
        this.scheduleTimeAdapter = scheduleTimeAdapter
    }

    override fun onInit() {
        scheduleTimeAdapter.setOnScheduleTimeEditListener(::selectScheduleTime)

        requireViewBinding().checkBoxScheduleTimeCourseTimeSame.isChecked = requireViewModel().scheduleTimeCourseTimeSame
        requireViewBinding().sliderScheduleCourseCostTime.value = requireViewModel().courseCostTime.toFloat()
        requireViewBinding().sliderScheduleBreakCostTime.value = requireViewModel().breakCostTime.toFloat()

        requireViewBinding().sliderScheduleCourseCostTime.setOnSlideValueSetListener {
            updateCourseCostTime(it.toInt(), false)
        }
        requireViewBinding().sliderScheduleBreakCostTime.setOnSlideValueSetListener {
            updateBreakCostTime(it.toInt(), false)
        }
        requireViewBinding().sliderScheduleTimeNum.setOnSlideValueSetListener {
            updateCourseNum(it.toInt(), false)
        }
        requireViewBinding().layoutScheduleTimeCourseTimeSame.setOnSingleClickListener {
            requireViewBinding().checkBoxScheduleTimeCourseTimeSame.toggle()
        }
        requireViewBinding().checkBoxScheduleTimeCourseTimeSame.setOnCheckedChangeListener { _, isChecked ->
            requireViewModel().scheduleTimeCourseTimeSame = isChecked
        }

        TimePickerDialog.setOnTimeSetListener(requireActivity().supportFragmentManager, requireActivity()) { tag, hourOfDay, minute ->
            onTimeSet(tag, hourOfDay, minute)
        }
    }

    private fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        if (tag != null) {
            if (tag.startsWith(TAG_SCHEDULE_TIME_START_PREFIX)) {
                tag.substringAfter(TAG_SCHEDULE_TIME_START_PREFIX).toIntOrNull()?.let { i ->
                    requireViewModel().apply {
                        val times = editSchedule.times
                        if (scheduleTimeCourseTimeSame) {
                            val courseCostTime = courseCostTime
                            val breakCostTime = breakCostTime
                            var last: ScheduleTime? = null
                            for (j in i until times.size) {
                                if (last != null) {
                                    last.endTimeMove(breakCostTime).let {
                                        times[j].startHour = it.first
                                        times[j].startMinute = it.second
                                    }
                                } else {
                                    times[i].startHour = hourOfDay
                                    times[i].startMinute = minute
                                }
                                last?.endTimeMove(breakCostTime)?.let {
                                    times[j].startHour = it.first
                                    times[j].startMinute = it.second
                                }
                                times[j].setDuration(courseCostTime)
                                last = times[j]
                            }
                            scheduleTimeAdapter.notifyItemRangeChanged(i, times.size - i)
                        } else {
                            times[i].startHour = hourOfDay
                            times[i].startMinute = minute
                            scheduleTimeAdapter.notifyItemChanged(i)
                        }
                    }
                }
            } else if (tag.startsWith(TAG_SCHEDULE_TIME_END_PREFIX)) {
                tag.substringAfter(TAG_SCHEDULE_TIME_END_PREFIX).toIntOrNull()?.let { i ->
                    requireViewModel().editSchedule.times[i].apply {
                        endHour = hourOfDay
                        endMinute = minute
                        scheduleTimeAdapter.notifyItemChanged(i)
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCourseCostTime(minute: Int, viewInit: Boolean) {
        if (!viewInit) {
            val breakCostTime = requireViewModel().breakCostTime
            val times = requireViewModel().editSchedule.times
            var last: ScheduleTime? = null
            for (time in times) {
                last?.endTimeMove(breakCostTime)?.let {
                    time.startHour = it.first
                    time.startMinute = it.second
                }
                time.setDuration(minute)
                last = time
            }
            requireViewModel().courseCostTime = minute
            scheduleTimeAdapter.notifyDataSetChanged()
        }
        requireViewBinding().textViewScheduleCourseCostTime.text = requireActivity().getString(R.string.minute, minute)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateBreakCostTime(minute: Int, viewInit: Boolean) {
        if (!viewInit) {
            val courseCostTime = requireViewModel().courseCostTime
            val times = requireViewModel().editSchedule.times
            var last: ScheduleTime? = null
            for (time in times) {
                last?.endTimeMove(minute)?.let {
                    time.startHour = it.first
                    time.startMinute = it.second
                }
                time.setDuration(courseCostTime)
                last = time
            }
            requireViewModel().breakCostTime = minute
            scheduleTimeAdapter.notifyDataSetChanged()
        }
        requireViewBinding().textViewScheduleBreakCostTime.text = requireActivity().getString(R.string.minute, minute)
    }

    fun updateCourseNum(num: Int, viewInit: Boolean) {
        if (!viewInit) {
            val courseCostTime = requireViewModel().courseCostTime
            val breakCostTime = requireViewModel().breakCostTime
            val times = requireViewModel().editSchedule.times.toMutableList()
            var differ = num - times.size
            if (differ > 0) {
                while (differ-- > 0) {
                    times.add(times.last().createNew(breakCostTime, courseCostTime))
                }
            } else {
                while (differ++ < 0) {
                    times.removeLast()
                }
            }
            times.let {
                requireViewModel().editSchedule.times = it
                scheduleTimeAdapter.submitList(it)
            }
            requireViewBinding().textViewScheduleTimeNum.text = requireActivity().getString(R.string.course_num, requireViewModel().editSchedule.times.size)
        } else {
            requireViewBinding().textViewScheduleTimeNum.text = requireActivity().getString(R.string.course_num, num)
        }
    }

    private fun selectScheduleTime(index: Int, hour: Int, minute: Int, isStart: Boolean) {
        if (!isStart && requireViewModel().scheduleTimeCourseTimeSame) {
            requireViewBinding().layoutScheduleEdit.showSnackBar(R.string.course_cost_time_same_mode_warning)
        } else {
            TimePickerDialog.showDialog(
                requireActivity().supportFragmentManager,
                (if (isStart) TAG_SCHEDULE_TIME_START_PREFIX else TAG_SCHEDULE_TIME_END_PREFIX) + index,
                hour, minute, true
            )
        }
    }
}