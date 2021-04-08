package tool.xfy9326.schedule.utils.view

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.databinding.DialogEditTextBinding
import tool.xfy9326.schedule.kt.getText
import tool.xfy9326.schedule.kt.show
import tool.xfy9326.schedule.kt.showShortToast
import tool.xfy9326.schedule.tools.MaterialColorHelper

object DialogUtils {
    fun showCalendarSyncAttentionDialog(activity: AppCompatActivity, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.calendar_sync)
            setMessage(R.string.calendar_sync_attention)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm()
            }
            setCancelable(false)
        }.show(activity)
    }

    fun showNewScheduleNameDialog(activity: AppCompatActivity, onConfirm: (String) -> Unit) {
        val dialogViewBinding = DialogEditTextBinding.inflate(activity.layoutInflater)
        dialogViewBinding.layoutDialogText.setHint(R.string.new_schedule_name_title)
        dialogViewBinding.editTextDialogText.setText(R.string.new_schedule_name)

        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.create_new_schedule)
            setView(dialogViewBinding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                dialogViewBinding.editTextDialogText.clearFocus()
                val text = dialogViewBinding.editTextDialogText.text.getText()
                if (text == null) {
                    activity.showShortToast(R.string.schedule_name_empty_error)
                } else {
                    onConfirm(text)
                }
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                dialogViewBinding.editTextDialogText.clearFocus()
            }
        }.show(activity)
    }

    fun showScheduleSelectDialog(
        activity: AppCompatActivity,
        @StringRes titleId: Int,
        schedules: List<Schedule.Min>,
        onSelect: (name: String, id: Long) -> Unit,
    ) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(titleId)
            setItems(schedules.map { it.name }.toTypedArray()) { _, index ->
                onSelect(schedules[index].name, schedules[index].scheduleId)
            }
            setNegativeButton(android.R.string.cancel, null)
        }.show(activity)
    }

    fun showOverwriteScheduleAttentionDialog(activity: AppCompatActivity, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.import_course_to_current_schedule)
            setMessage(R.string.import_course_to_current_schedule_msg)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm()
            }
            setNegativeButton(android.R.string.cancel, null)
        }.show(activity)
    }

    fun showCancelScheduleImportDialog(activity: AppCompatActivity, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.importing_courses)
            setMessage(R.string.importing_courses_msg)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm()
            }
            setNegativeButton(android.R.string.cancel, null)
        }.show(activity)
    }

    fun showColorPickerDialog(activity: AppCompatActivity, @StringRes titleId: Int, color: Int, dialogId: Int = 0) {
        ColorPickerDialog.newBuilder().apply {
            setColor(color)
            setDialogTitle(titleId)
            setDialogId(dialogId)
            setShowAlphaSlider(false)
            setPresets(MaterialColorHelper.all())
        }.show(activity)
    }

    fun showEULADialog(activity: AppCompatActivity, content: String, cancelable: Boolean = true, onOperate: (agree: Boolean) -> Unit = {}) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.eula_license)
            setMessage(content)
            setCancelable(cancelable)
            if (!cancelable) {
                setPositiveButton(R.string.agree) { _, _ ->
                    onOperate(true)
                }
                setNegativeButton(R.string.disagree) { _, _ ->
                    onOperate(false)
                }
            }
        }.show(activity)
    }

    fun showOpenSourceLicenseDialog(activity: AppCompatActivity, content: String) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.open_source_license)
            setMessage(content)
        }.show(activity)
    }
}