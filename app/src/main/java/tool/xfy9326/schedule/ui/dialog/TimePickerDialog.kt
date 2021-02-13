package tool.xfy9326.schedule.ui.dialog

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import tool.xfy9326.schedule.kt.requireOwner

class TimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener {
    companion object {
        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_HOUR = "EXTRA_HOUR"
        private const val EXTRA_MINUTE = "EXTRA_MINUTE"
        private const val EXTRA_IS_24_HOUR_VIEW = "EXTRA_IS_24_HOUR_VIEW"

        fun showDialog(fragmentManager: FragmentManager, tag: String?, hour: Int, minute: Int, is24HourView: Boolean) {
            TimePickerDialog().apply {
                arguments = bundleOf(
                    EXTRA_TAG to tag,
                    EXTRA_HOUR to hour,
                    EXTRA_MINUTE to minute,
                    EXTRA_IS_24_HOUR_VIEW to is24HourView
                )
            }.show(fragmentManager, tag)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        TimePickerDialog(
            requireContext(),
            this,
            requireArguments().getInt(EXTRA_HOUR),
            requireArguments().getInt(EXTRA_MINUTE),
            requireArguments().getBoolean(EXTRA_IS_24_HOUR_VIEW)
        )

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        requireOwner<OnTimeSetListener>()?.onTimeSet(requireArguments().getString(EXTRA_TAG), hourOfDay, minute)
    }

    interface OnTimeSetListener {
        fun onTimeSet(tag: String?, hourOfDay: Int, minute: Int)
    }
}