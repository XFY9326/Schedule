package tool.xfy9326.schedule.ui.dialog

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner

class TimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener {
    companion object {
        private val DIALOG_TAG = TimePickerDialog::class.java.simpleName

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

        fun setOnTimeSetListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, block: (tag: String?, hourOfDay: Int, minute: Int) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(bundle.getString(EXTRA_TAG), bundle.getInt(EXTRA_HOUR), bundle.getInt(EXTRA_MINUTE))
            }
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
        setFragmentResult(DIALOG_TAG, bundleOf(
            EXTRA_TAG to requireArguments().getString(EXTRA_TAG),
            EXTRA_HOUR to hourOfDay,
            EXTRA_MINUTE to minute
        ))
    }
}