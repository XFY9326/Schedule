package tool.xfy9326.schedule.ui.dialog

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
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.course_import_success_title)
        val msgHighLight = getString(R.string.course_import_success_attention_highlight)
        val msgOriginal = getString(R.string.course_import_success_attention, msgHighLight)
        val start = msgOriginal.indexOf(msgHighLight)
        val end = start + msgHighLight.length

        setMessage(
            SpannableString(msgOriginal).apply {
                val span = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TypefaceSpan(Typeface.DEFAULT_BOLD)
                } else {
                    StyleSpan(Typeface.BOLD)
                }
                setSpan(span, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        )
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