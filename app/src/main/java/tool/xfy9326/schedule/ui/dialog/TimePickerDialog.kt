package tool.xfy9326.schedule.ui.dialog

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.requireOwner

class TimePickerDialog : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    companion object {
        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_HOUR = "EXTRA_HOUR"
        private const val EXTRA_MINUTE = "EXTRA_MINUTE"
        private const val EXTRA_IS_24_HOUR_VIEW = "EXTRA_IS_24_HOUR_VIEW"

        fun showDialog(fragmentManager: FragmentManager, tag: String?, hour: Int, minute: Int, is24HourView: Boolean) {
            TimePickerDialog().apply {
                arguments = buildBundle {
                    putString(EXTRA_TAG, tag)
                    putInt(EXTRA_HOUR, hour)
                    putInt(EXTRA_MINUTE, minute)
                    putBoolean(EXTRA_IS_24_HOUR_VIEW, is24HourView)
                }
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