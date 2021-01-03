package tool.xfy9326.schedule.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.ScheduleTimeAdapter
import tool.xfy9326.schedule.ui.dialog.DatePickerDialog
import tool.xfy9326.schedule.ui.dialog.TimePickerDialog
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel
import tool.xfy9326.schedule.utils.DialogUtils
import java.text.SimpleDateFormat
import java.util.*

class ScheduleEditActivity : ViewModelActivity<ScheduleEditViewModel, ActivityScheduleEditBinding>(),
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, ColorPickerDialogListener {

    companion object {
        const val INTENT_EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
        const val INTENT_EXTRA_IS_CURRENT_SCHEDULE = "EXTRA_IS_CURRENT_SCHEDULE"

        private const val TAG_SCHEDULE_START_DATE = "SCHEDULE_START_DATE"
        private const val TAG_SCHEDULE_END_DATE = "SCHEDULE_END_DATE"

        private const val TAG_SCHEDULE_TIME_START_PREFIX = "TAG_SCHEDULE_TIME_START_"
        private const val TAG_SCHEDULE_TIME_END_PREFIX = "TAG_SCHEDULE_TIME_END_"
    }

    private val scheduleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var scheduleTimeAdapter: ScheduleTimeAdapter

    override fun onPrepare(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        setSupportActionBar(viewBinding.toolBarScheduleEdit.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scheduleTimeAdapter = ScheduleTimeAdapter()
        viewBinding.recyclerViewScheduleTimeList.adapter = scheduleTimeAdapter

        viewModel.requestDBScheduleData(intent.getLongExtra(INTENT_EXTRA_SCHEDULE_ID, 0))
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        viewModel.scheduleData.observe(this, ::applyScheduleToView)
        viewModel.scheduleSaveComplete.observeEvent(this, observer = ::onScheduleSaved)
        viewModel.selectScheduleDate.observeEvent(this) {
            DatePickerDialog.showDialog(
                supportFragmentManager,
                if (it.first) TAG_SCHEDULE_START_DATE else TAG_SCHEDULE_END_DATE,
                it.second,
                it.third.calWeekDay
            )
        }
        viewModel.scheduleSaveFailed.observeEvent(this) {
            viewBinding.layoutScheduleEdit.showShortSnackBar(it.getText(this))
        }
    }

    override fun onInitView(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        viewBinding.recyclerViewScheduleTimeList.itemAnimator = null
        scheduleTimeAdapter.setOnScheduleTimeEditListener(::selectScheduleTime)

        viewBinding.checkBoxScheduleTimeCourseTimeSame.isChecked = viewModel.scheduleTimeCourseTimeSame
        viewBinding.sliderScheduleCourseCostTime.value = viewModel.courseCostTime.toFloat()
        viewBinding.sliderScheduleBreakCostTime.value = viewModel.breakCostTime.toFloat()

        viewBinding.buttonScheduleStartDateEdit.setOnClickListener {
            viewModel.selectScheduleDate(true, viewModel.editSchedule.startDate)
        }
        viewBinding.buttonScheduleEndDateEdit.setOnClickListener {
            viewModel.selectScheduleDate(false, viewModel.editSchedule.endDate)
        }
        viewBinding.sliderScheduleCourseCostTime.setOnSlideValueSetListener {
            updateCourseCostTime(it.toInt(), false)
        }
        viewBinding.sliderScheduleBreakCostTime.setOnSlideValueSetListener {
            updateBreakCostTime(it.toInt(), false)
        }
        viewBinding.sliderScheduleTimeNum.setOnSlideValueSetListener {
            updateCourseNum(it.toInt(), false)
        }
        viewBinding.checkBoxScheduleTimeCourseTimeSame.setOnCheckedChangeListener { _, isChecked ->
            viewModel.scheduleTimeCourseTimeSame = isChecked
        }
        viewBinding.buttonScheduleColorEdit.setOnClickListener {
            DialogUtils.showColorPickerDialog(this, R.string.schedule_color_edit, viewModel.editSchedule.color)
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        requireViewModel().apply {
            if (editSchedule.color != color) {
                editSchedule.color = color
                updateScheduleColor(color)
            }
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_schedule_edit, menu)
        if (!requireViewModel().isEdit) {
            menu?.findItem(R.id.menu_scheduleEditDelete)?.let {
                it.isVisible = false
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun onScheduleSaved(newScheduleId: Long) {
        if (intent.getLongExtra(INTENT_EXTRA_SCHEDULE_ID, 0) == 0L) {
            requireViewBinding().toolBarScheduleEdit.toolBarGeneral.menu.findItem(R.id.menu_scheduleEditDelete)?.let { menuItem ->
                menuItem.isVisible = true
            }
            intent.putExtra(INTENT_EXTRA_SCHEDULE_ID, newScheduleId)
        }
        requireViewBinding().layoutScheduleEdit.showShortSnackBar(R.string.save_success)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_scheduleEditSave -> requireViewModel().apply {
                requireViewBinding().layoutScheduleEdit.apply {
                    clearFocus()
                    hideKeyboard(windowToken)
                }
                updateTextData()
                saveSchedule()
            }
            R.id.menu_scheduleEditDelete -> deleteScheduleAttention()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateTextData()
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        updateTextData()
        if (requireViewModel().hasDataChanged()) {
            Snackbar.make(requireViewBinding().layoutScheduleEdit, R.string.ask_whether_exit_without_save, Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.RED)
                .setAction(android.R.string.ok) {
                    super.onBackPressed()
                }.show()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateTextData() {
        requireViewModel().apply {
            editSchedule.name = requireViewBinding().editTextScheduleName.text.toString()
            editSchedule.refreshMaxWeekNum()
        }
    }

    private fun applyScheduleToView(schedule: Schedule) {
        requireViewBinding().apply {
            scheduleTimeAdapter.submitList(schedule.times.toList())

            editTextScheduleName.setText(schedule.name)
            sliderScheduleTimeNum.value = schedule.times.size.toFloat()

            updateScheduleDate(true, schedule.startDate, false)
            updateScheduleDate(false, schedule.endDate, false)
            updateScheduleColor(schedule.color)

            requireViewModel().apply {
                updateCourseCostTime(courseCostTime, true)
                updateBreakCostTime(breakCostTime, true)
                updateCourseNum(schedule.times.size, true)
            }
        }
    }

    override fun onDateSet(tag: String?, date: Date) {
        if (tag == TAG_SCHEDULE_START_DATE) {
            updateScheduleDate(true, date, true)
        } else if (tag == TAG_SCHEDULE_END_DATE) {
            updateScheduleDate(false, date, true)
        }
    }

    private fun updateScheduleColor(color: Int) {
        requireViewBinding().buttonScheduleColorEdit.imageTintList = ColorStateList.valueOf(color)
    }

    private fun updateScheduleDate(isStart: Boolean, date: Date, updateEdit: Boolean) {
        if (isStart) {
            if (updateEdit) requireViewModel().editSchedule.startDate = date
            requireViewBinding().textViewScheduleStartDate
        } else {
            if (updateEdit) requireViewModel().editSchedule.endDate = date
            requireViewBinding().textViewScheduleEndDate
        }.text = scheduleDateFormat.format(date)
    }

    private fun updateCourseCostTime(minute: Int, viewInit: Boolean) {
        if (!viewInit) {
            val breakCostTime = requireViewModel().breakCostTime
            val times = requireViewModel().editSchedule.times.toList()
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
        requireViewBinding().textViewScheduleCourseCostTime.text = getString(R.string.minute, minute)
    }

    private fun updateBreakCostTime(minute: Int, viewInit: Boolean) {
        if (!viewInit) {
            val courseCostTime = requireViewModel().courseCostTime
            val times = requireViewModel().editSchedule.times.toList()
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
        requireViewBinding().textViewScheduleBreakCostTime.text = getString(R.string.minute, minute)
    }

    private fun updateCourseNum(num: Int, viewInit: Boolean) {
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
            times.toTypedArray().let {
                requireViewModel().editSchedule.times = it
                scheduleTimeAdapter.submitList(it.toList())
            }
        }
        requireViewBinding().textViewScheduleTimeNum.text = getString(R.string.course_num, requireViewModel().editSchedule.times.size)
    }

    private fun selectScheduleTime(index: Int, hour: Int, minute: Int, isStart: Boolean) {
        if (!isStart && requireViewModel().scheduleTimeCourseTimeSame) {
            requireViewBinding().layoutScheduleEdit.showShortSnackBar(R.string.course_cost_time_same_mode_warning)
        } else {
            TimePickerDialog.showDialog(
                supportFragmentManager,
                (if (isStart) TAG_SCHEDULE_TIME_START_PREFIX else TAG_SCHEDULE_TIME_END_PREFIX) + index,
                hour, minute, true
            )
        }
    }

    override fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
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

    private fun deleteScheduleAttention() {
        if (intent.getBooleanExtra(INTENT_EXTRA_IS_CURRENT_SCHEDULE, false)) {
            requireViewBinding().layoutScheduleEdit.showShortSnackBar(R.string.unable_to_delete_using_schedule)
        } else {
            if (requireViewModel().isEdit) {
                val schedule = requireViewModel().editSchedule
                MaterialAlertDialogBuilder(this).apply {
                    setTitle(getString(R.string.ask_whether_delete_schedule, schedule.name))
                    setMessage(getString(R.string.ask_whether_delete_schedule_msg))
                    setPositiveButton(R.string.delete) { _, _ ->
                        requireViewModel().deleteSchedule(schedule)
                        showShortToast(R.string.delete_success)
                        finish()
                    }
                    setNegativeButton(android.R.string.cancel, null)
                }.show(this)
            }
        }
    }
}