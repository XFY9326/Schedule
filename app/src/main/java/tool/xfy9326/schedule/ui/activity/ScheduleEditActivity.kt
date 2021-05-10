package tool.xfy9326.schedule.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.activity.module.ScheduleTermEditModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleTimeEditModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleTimeImportModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleWeekStartModule
import tool.xfy9326.schedule.ui.adapter.ScheduleTimeAdapter
import tool.xfy9326.schedule.ui.dialog.DatePickerDialog
import tool.xfy9326.schedule.ui.dialog.TimePickerDialog
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel
import tool.xfy9326.schedule.utils.view.DialogUtils
import java.util.*

class ScheduleEditActivity : ViewModelActivity<ScheduleEditViewModel, ActivityScheduleEditBinding>(),
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, ColorPickerDialogListener {

    companion object {
        const val INTENT_EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
        const val INTENT_EXTRA_IS_CURRENT_SCHEDULE = "EXTRA_IS_CURRENT_SCHEDULE"
    }

    override val vmClass = ScheduleEditViewModel::class

    private val scheduleTimeImportModule = ScheduleTimeImportModule(this)
    private val scheduleTermEditModule = ScheduleTermEditModule(this)
    private val scheduleTimeEditModule = ScheduleTimeEditModule(this)
    private val scheduleWeekStartModule = ScheduleWeekStartModule(this)

    private lateinit var scheduleTimeAdapter: ScheduleTimeAdapter

    override fun onCreateViewBinding() = ActivityScheduleEditBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        setSupportActionBar(viewBinding.toolBarScheduleEdit.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scheduleTimeAdapter = ScheduleTimeAdapter()
        viewBinding.recyclerViewScheduleTimeList.adapter = scheduleTimeAdapter
        scheduleTimeImportModule.bindScheduleTimeAdapter(scheduleTimeAdapter)
        scheduleTimeEditModule.bindScheduleTimeAdapter(scheduleTimeAdapter)

        viewModel.requestDBScheduleData(intent.getLongExtra(INTENT_EXTRA_SCHEDULE_ID, 0))
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        viewModel.scheduleData.observe(this, ::applyScheduleToView)
        viewModel.scheduleSaveComplete.observeEvent(this, observer = ::onScheduleSaved)
        viewModel.scheduleSaveFailed.observeEvent(this) {
            viewBinding.layoutScheduleEdit.showSnackBar(it.getText(this))
        }

        scheduleTimeImportModule.init()
        scheduleTimeEditModule.init()
        scheduleTermEditModule.init()
        scheduleWeekStartModule.init()
    }

    override fun onInitView(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        viewBinding.recyclerViewScheduleTimeList.itemAnimator = null

        viewBinding.buttonScheduleColorEdit.setOnSingleClickListener {
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
        requireViewBinding().layoutScheduleEdit.showSnackBar(R.string.save_success)
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
            R.id.menu_scheduleEditImport -> scheduleTimeImportModule.importScheduleTime()
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
            Snackbar.make(requireViewBinding().layoutScheduleEdit, R.string.ask_whether_exit_without_save, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.RED)
                .setAction(android.R.string.ok) {
                    super.onBackPressed()
                }.show()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateTextData() {
        requireViewModel().editSchedule.name = requireViewBinding().editTextScheduleName.text.toString()
    }

    private fun applyScheduleToView(schedule: Schedule) {
        requireViewBinding().apply {
            scheduleTimeAdapter.submitList(schedule.times)

            editTextScheduleName.setText(schedule.name)
            sliderScheduleTimeNum.value = schedule.times.size.toFloat()
            scheduleWeekStartModule.updateWeekStartText(schedule.weekStart)

            scheduleTermEditModule.updateScheduleDate(true, schedule.startDate, false)
            scheduleTermEditModule.updateScheduleDate(false, schedule.endDate, false)
            updateScheduleColor(schedule.color)

            requireViewModel().apply {
                scheduleTimeEditModule.updateCourseCostTime(courseCostTime, true)
                scheduleTimeEditModule.updateBreakCostTime(breakCostTime, true)
                scheduleTimeEditModule.updateCourseNum(schedule.times.size, true)
            }
        }
    }

    override fun onDateSet(tag: String?, date: Date) {
        scheduleTermEditModule.onDateSet(tag, date)
    }

    private fun updateScheduleColor(color: Int) {
        requireViewBinding().buttonScheduleColorEdit.imageTintList = ColorStateList.valueOf(color)
    }

    override fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        scheduleTimeEditModule.onTimeSet(tag, hourOfDay, minute)
    }

    private fun deleteScheduleAttention() {
        if (intent.getBooleanExtra(INTENT_EXTRA_IS_CURRENT_SCHEDULE, false)) {
            requireViewBinding().layoutScheduleEdit.showSnackBar(R.string.unable_to_delete_using_schedule)
        } else {
            if (requireViewModel().isEdit) {
                val schedule = requireViewModel().editSchedule
                MaterialAlertDialogBuilder(this).apply {
                    setTitle(getString(R.string.ask_whether_delete_schedule, schedule.name))
                    setMessage(getString(R.string.ask_whether_delete_schedule_msg))
                    setPositiveButton(R.string.delete) { _, _ ->
                        requireViewModel().deleteSchedule(schedule)
                        showToast(R.string.delete_success)
                        finish()
                    }
                    setNegativeButton(android.R.string.cancel, null)
                }.show(this)
            }
        }
    }
}