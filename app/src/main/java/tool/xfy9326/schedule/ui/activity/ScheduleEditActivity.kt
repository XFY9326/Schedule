package tool.xfy9326.schedule.ui.activity

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import io.github.xfy9326.atools.core.hideKeyboard
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.ui.resume
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import io.github.xfy9326.atools.ui.show
import io.github.xfy9326.atools.ui.showToast
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.EditError.Companion.getText
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.databinding.ActivityScheduleEditBinding
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.activity.module.ScheduleTermEditModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleTimeEditModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleTimeImportModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleWeekStartModule
import tool.xfy9326.schedule.ui.vm.ScheduleEditViewModel
import tool.xfy9326.schedule.utils.consumeSystemBarInsets
import tool.xfy9326.schedule.utils.showSnackBar
import tool.xfy9326.schedule.utils.view.DialogUtils

class ScheduleEditActivity : ViewModelActivity<ScheduleEditViewModel, ActivityScheduleEditBinding>(), ColorPickerDialogListener {

    companion object {
        private const val INTENT_EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
        private const val INTENT_EXTRA_IS_CURRENT_SCHEDULE = "EXTRA_IS_CURRENT_SCHEDULE"

        fun startActivity(context: Context, scheduleId: Long, isCurrentSchedule: Boolean) {
            context.startActivity<ScheduleEditActivity> {
                putExtra(INTENT_EXTRA_SCHEDULE_ID, scheduleId)
                putExtra(INTENT_EXTRA_IS_CURRENT_SCHEDULE, isCurrentSchedule)
            }
        }
    }

    override val vmClass = ScheduleEditViewModel::class

    private val scheduleTimeImportModule = ScheduleTimeImportModule(this)
    private val scheduleTermEditModule = ScheduleTermEditModule(this)
    private val scheduleTimeEditModule = ScheduleTimeEditModule(this)
    private val scheduleWeekStartModule = ScheduleWeekStartModule(this)

    override fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: ScheduleEditViewModel) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        onBackPressedDispatcher.addCallback(this, true, this::onBackPressed)
    }

    override fun onCreateViewBinding() = ActivityScheduleEditBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        setSupportActionBar(viewBinding.toolBarScheduleEdit.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.requestDBScheduleData(intent.getLongExtra(INTENT_EXTRA_SCHEDULE_ID, 0))
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        viewModel.scheduleData.observe(this, ::applyScheduleToView)
        viewModel.scheduleSaveComplete.observeEvent(this, observer = ::onScheduleSaved)
        viewModel.scheduleSaveFailed.observeEvent(this) {
            viewBinding.layoutScheduleEdit.showSnackBar(it.getText(this))
        }

        scheduleTimeEditModule.init()

        scheduleTimeImportModule.bindScheduleTimeAdapter(scheduleTimeEditModule.listAdapter)

        scheduleTimeImportModule.init()
        scheduleTermEditModule.init()
        scheduleWeekStartModule.init()
    }

    override fun onInitView(viewBinding: ActivityScheduleEditBinding, viewModel: ScheduleEditViewModel) {
        viewBinding.layoutScheduleTimeEdit.recyclerViewScheduleTimeList.itemAnimator = null

        viewBinding.buttonScheduleColorEdit.setOnSingleClickListener {
            DialogUtils.showColorPickerDialog(this, R.string.schedule_color_edit, viewModel.editSchedule.color)
        }

        viewBinding.layoutScheduleEditContent.consumeSystemBarInsets(bottom = true)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_schedule_edit, menu)
        if (!requireViewModel().isEdit) {
            menu.findItem(R.id.menu_scheduleEditDelete)?.let {
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateTextData()
        super.onSaveInstanceState(outState)
    }

    private fun onBackPressed(callback: OnBackPressedCallback) {
        updateTextData()
        if (requireViewModel().hasDataChanged()) {
            Snackbar.make(requireViewBinding().layoutScheduleEdit, R.string.ask_whether_exit_without_save, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.RED)
                .setAction(android.R.string.ok) {
                    callback.resume(onBackPressedDispatcher)
                }.show()
        } else {
            callback.resume(onBackPressedDispatcher)
        }
    }

    private fun updateTextData() {
        requireViewModel().editSchedule.name = requireViewBinding().editTextScheduleName.text.toString()
    }

    private fun applyScheduleToView(schedule: Schedule) {
        requireViewBinding().apply {
            scheduleTimeEditModule.listAdapter.submitList(schedule.times)

            editTextScheduleName.setText(schedule.name)
            requireViewBinding().layoutScheduleTimeEdit.sliderScheduleTimeNum.value = schedule.times.size.toFloat()
            scheduleWeekStartModule.updateWeekStartText(schedule.weekStart)

            scheduleTermEditModule.updateScheduleDate(true, schedule.startDate, false)
            scheduleTermEditModule.updateScheduleDate(false, schedule.endDate, false)
            updateScheduleColor(schedule.color)

            scheduleTimeEditModule.initUpdateAll(schedule)
        }
    }

    private fun updateScheduleColor(color: Int) {
        requireViewBinding().buttonScheduleColorEdit.imageTintList = ColorStateList.valueOf(color)
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