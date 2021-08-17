package tool.xfy9326.schedule.ui.dialog

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lib.xfy9326.android.kit.requireOwner
import lib.xfy9326.kit.EMPTY
import tool.xfy9326.schedule.R

class ScheduleImportSuccessDialog : DialogFragment() {
    companion object {
        private val DIALOG_TAG = ScheduleImportSuccessDialog::class.simpleName
        private const val EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"

        fun showDialog(fragmentManager: FragmentManager, scheduleId: Long) {
            ScheduleImportSuccessDialog().apply {
                arguments = bundleOf(
                    EXTRA_SCHEDULE_ID to scheduleId
                )
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun getImportSuccessMsg(context: Context, showEditMsg: Boolean): SpannableString {
            val msgEditHighlight = if (showEditMsg) {
                context.getString(R.string.course_import_success_attention_edit_schedule_highlight)
            } else {
                EMPTY
            }
            val msgNecessaryHighlight = context.getString(R.string.course_import_success_attention_necessary_highlight)
            val msgOriginal = context.getString(R.string.course_import_success_attention, msgNecessaryHighlight)
            val start = msgOriginal.indexOf(msgNecessaryHighlight)
            val end = start + msgNecessaryHighlight.length

            return SpannableString(msgEditHighlight + msgOriginal).apply {
                val span = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TypefaceSpan(Typeface.DEFAULT_BOLD)
                } else {
                    StyleSpan(Typeface.BOLD)
                }
                if (msgEditHighlight.isNotEmpty()) {
                    setSpan(span, 0, msgEditHighlight.length, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE)
                }
                setSpan(span, msgEditHighlight.length + start, msgEditHighlight.length + end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.course_import_success_title)
        setMessage(getImportSuccessMsg(requireContext(), true))
        setPositiveButton(R.string.edit_schedule_now) { _, _ ->
            requireOwner<OnScheduleImportSuccessListener>()?.onEditScheduleNow(requireArguments().getLong(EXTRA_SCHEDULE_ID))
        }
        setNegativeButton(R.string.edit_schedule_later) { _, _ ->
            requireOwner<OnScheduleImportSuccessListener>()?.onEditScheduleLater()
        }
    }.create()

    override fun onStart() {
        super.onStart()
        requireDialog().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    interface OnScheduleImportSuccessListener {
        fun onEditScheduleNow(scheduleId: Long)

        fun onEditScheduleLater()
    }
}