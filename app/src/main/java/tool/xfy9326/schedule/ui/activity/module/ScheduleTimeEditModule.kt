package tool.xfy9326.schedule.ui.activity.module

import android.annotation.SuppressLint
import androidx.recyclerview.widget.ListAdapter
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.databinding.LayoutScheduleTimeBinding
import tool.xfy9326.schedule.ui.activity.ScheduleEditActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.adapter.ScheduleTimeAdapter
import tool.xfy9326.schedule.ui.dialog.TimePickerDialog
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel
import tool.xfy9326.schedule.utils.showSnackBar

class ScheduleTimeEditModule(activity: ScheduleEditActivity) :
    AbstractViewModelActivityModule<ScheduleEditViewModel, ActivityScheduleEditBinding, ScheduleEditActivity>(activity) {
    companion object {
        private const val TAG_SCHEDULE_TIME_START_PREFIX = "TAG_SCHEDULE_TIME_START_"
        private const val TAG_SCHEDULE_TIME_END_PREFIX = "TAG_SCHEDULE_TIME_END_"
    }

    private lateinit var scheduleTimeAdapter: ScheduleTimeAdapter
    val listAdapter: ListAdapter<ScheduleTime, *>
        get() = scheduleTimeAdapter

    override fun onInit() {
        val viewBinding = requireViewBinding().layoutScheduleTimeEdit

        scheduleTimeAdapter = ScheduleTimeAdapter()
        scheduleTimeAdapter.setOnScheduleTimeEditListener(::selectScheduleTime)
        viewBinding.recyclerViewScheduleTimeList.adapter = scheduleTimeAdapter

        viewBinding.checkBoxScheduleTimeCourseTimeSame.isChecked = requireViewModel().scheduleTimeCourseTimeSame
        viewBinding.sliderScheduleCourseCostTime.value = requireViewModel().courseCostTime.toFloat()
        viewBinding.sliderScheduleBreakCostTime.value = requireViewModel().breakCostTime.toFloat()

        viewBinding.sliderScheduleCourseCostTime.setOnSlideValueSetListener {
            updateCourseCostTime(viewBinding, it.toInt(), false)
        }
        viewBinding.sliderScheduleBreakCostTime.setOnSlideValueSetListener {
            updateBreakCostTime(viewBinding, it.toInt(), false)
        }
        viewBinding.sliderScheduleTimeNum.setOnSlideValueSetListener {
            updateCourseNum(viewBinding, it.toInt(), false)
        }
        viewBinding.layoutScheduleTimeCourseTimeSame.setOnSingleClickListener {
            viewBinding.checkBoxScheduleTimeCourseTimeSame.toggle()
        }
        viewBinding.checkBoxScheduleTimeCourseTimeSame.setOnCheckedChangeListener { _, isChecked ->
            requireViewModel().scheduleTimeCourseTimeSame = isChecked
        }

        TimePickerDialog.setOnTimeSetListener(requireActivity().supportFragmentManager, requireActivity(), ::onTimeSet)
    }

    private fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        if (tag == null) return
        if (tag.startsWith(TAG_SCHEDULE_TIME_START_PREFIX)) {
            tag.substringAfter(TAG_SCHEDULE_TIME_START_PREFIX).toIntOrNull()?.let { i ->
                requireViewModel().apply {
                    val times = editSchedule.times
                    if (scheduleTimeCourseTimeSame) {
                        updateStartTime(this, i, hourOfDay, minute)
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

    private fun updateStartTime(viewModel: ScheduleEditViewModel, index: Int, hourOfDay: Int, minute: Int) {
        val times = viewModel.editSchedule.times
        val courseCostTime = viewModel.courseCostTime
        val breakCostTime = viewModel.breakCostTime
        var last: ScheduleTime? = null
        for (j in index until times.size) {
            last?.endTimeMove(breakCostTime)?.let {
                times[j].startHour = it.first
                times[j].startMinute = it.second
            } ?: {
                times[index].startHour = hourOfDay
                times[index].startMinute = minute
            }
            last?.endTimeMove(breakCostTime)?.let {
                times[j].startHour = it.first
                times[j].startMinute = it.second
            }
            times[j].setDuration(courseCostTime)
            last = times[j]
        }
    }

    fun initUpdateAll(schedule: Schedule) {
        val viewBinding = requireViewBinding().layoutScheduleTimeEdit
        requireViewModel().apply {
            updateCourseCostTime(viewBinding, courseCostTime, true)
            updateBreakCostTime(viewBinding, breakCostTime, true)
            updateCourseNum(viewBinding, schedule.times.size, true)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateCourseCostTime(viewBinding: LayoutScheduleTimeBinding, minute: Int, viewInit: Boolean) {
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
        viewBinding.textViewScheduleCourseCostTime.text = requireActivity().getString(R.string.minute, minute)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateBreakCostTime(viewBinding: LayoutScheduleTimeBinding, minute: Int, viewInit: Boolean) {
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
        viewBinding.textViewScheduleBreakCostTime.text = requireActivity().getString(R.string.minute, minute)
    }

    private fun updateCourseNum(viewBinding: LayoutScheduleTimeBinding, num: Int, viewInit: Boolean) {
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
            viewBinding.textViewScheduleTimeNum.text =
                requireActivity().getString(R.string.course_num, requireViewModel().editSchedule.times.size)
        } else {
            viewBinding.textViewScheduleTimeNum.text = requireActivity().getString(R.string.course_num, num)
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