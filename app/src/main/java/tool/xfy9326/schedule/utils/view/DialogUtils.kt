package tool.xfy9326.schedule.utils.view

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import io.github.xfy9326.atools.ui.getText
import io.github.xfy9326.atools.ui.show
import io.github.xfy9326.atools.ui.showToast
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.databinding.DialogEditTextBinding
import tool.xfy9326.schedule.tools.MaterialColorHelper
import tool.xfy9326.schedule.ui.activity.RawTextActivity
import tool.xfy9326.schedule.utils.NEW_LINE

object DialogUtils {
    fun showAdvancedFunctionDialog(context: Context, lifecycleOwner: LifecycleOwner, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.advanced_function_alert)
            setMessage(R.string.advanced_function_alert_msg)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm()
            }
        }.show(lifecycleOwner)
    }

    fun showEmptyWeekNumCourseAlertDialog(context: Context, lifecycleOwner: LifecycleOwner, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.has_empty_week_num_course)
            setMessage(R.string.has_empty_week_num_course_msg)
            setNegativeButton(R.string.do_it_later, null)
            setPositiveButton(R.string.go_now) { _, _ ->
                onConfirm()
            }
        }.show(lifecycleOwner)
    }

    fun showOnlineImportAttentionDialog(
        activity: AppCompatActivity,
        isStaticAttention: Boolean,
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null,
        onNeutral: (() -> Unit)? = null,
    ) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.online_course_import)
            if (isStaticAttention) {
                setMessage(R.string.online_course_import_attention)
            } else {
                setMessage(activity.getString(R.string.online_course_import_attention) + NEW_LINE + NEW_LINE + activity.getString(R.string.add_course_import_attention))
            }
            if (isStaticAttention) {
                setCancelable(false)
                setPositiveButton(R.string.has_read) { _, _ ->
                    onPositive?.invoke()
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    onNegative?.invoke()
                }
                setNeutralButton(R.string.disable_function) { _, _ ->
                    onNeutral?.invoke()
                }
            } else {
                setPositiveButton(android.R.string.ok, null)
                setNeutralButton(R.string.add_course_import_wiki) { _, _ ->
                    onNeutral?.invoke()
                }
            }
        }.show(activity)
    }

    fun showCalendarSyncAttentionDialog(activity: AppCompatActivity, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.calendar_sync_attention_dialog_title)
            setMessage(R.string.calendar_sync_attention_dialog_msg)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm()
            }
            setCancelable(false)
        }.show(activity)
    }

    fun showAddCourseImportAttentionDialog(activity: AppCompatActivity, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.add_course_import_attention_title)
            setMessage(R.string.add_course_import_attention)
            setPositiveButton(R.string.agree_policy) { _, _ ->
                onConfirm()
            }
            setNegativeButton(android.R.string.cancel, null)
            setCancelable(false)
        }.show(activity)
    }

    fun showNewScheduleNameDialog(activity: AppCompatActivity, onConfirm: (String) -> Unit) {
        val dialogViewBinding = DialogEditTextBinding.inflate(activity.layoutInflater)
        dialogViewBinding.textLayoutDialogText.setHint(R.string.new_schedule_name_title)
        dialogViewBinding.textLayoutDialogText.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
        dialogViewBinding.editTextDialogText.setText(R.string.new_schedule_name)

        MaterialAlertDialogBuilder(activity).apply {
            setTitle(R.string.create_new_schedule)
            setView(dialogViewBinding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                dialogViewBinding.editTextDialogText.clearFocus()
                val text = dialogViewBinding.editTextDialogText.text.getText()
                if (text == null) {
                    activity.showToast(R.string.schedule_name_empty_error)
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

    fun showEULADialog(activity: AppCompatActivity, isUpdate: Boolean, onOperate: (agree: Boolean) -> Unit = {}) {
        MaterialAlertDialogBuilder(activity).apply {
            val showText = if (isUpdate) {
                activity.getString(R.string.eula_license_update, activity.resources.getInteger(R.integer.eula_version))
            } else {
                activity.getString(R.string.eula_license_msg, activity.getString(R.string.app_name))
            }
            setTitle(R.string.eula_license)
            setMessage(showText)
            setCancelable(false)
            setPositiveButton(R.string.agree) { _, _ ->
                onOperate(true)
            }
            setNegativeButton(R.string.disagree) { _, _ ->
                onOperate(false)
            }
            setNeutralButton(R.string.eula_license_view, null)
        }.show(activity, onCreate = { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                    RawTextActivity.launch(activity, R.string.eula_license, R.raw.eula)
                }
            }
        })
    }
}