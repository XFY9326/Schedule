package tool.xfy9326.schedule.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import tool.xfy9326.schedule.tools.core.getSerializableCompat
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.Calendar
import java.util.Date

class DatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        private val DIALOG_TAG = DatePickerDialog::class.java.simpleName

        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_DATE = "EXTRA_DATE"
        private const val EXTRA_WEEK_START = "EXTRA_WEEK_START"

        fun showDialog(fragmentManager: FragmentManager, tag: String?, date: Date, calWeekStart: Int) {
            DatePickerDialog().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TAG, tag)
                    putInt(EXTRA_WEEK_START, calWeekStart)
                    putSerializable(EXTRA_DATE, date)
                }
            }.show(fragmentManager, tag)
        }

        fun setOnDateSetListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, block: (tag: String?, date: Date) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(bundle.getString(EXTRA_TAG), bundle.getSerializableCompat(EXTRA_DATE)!!)
            }
        }

        private fun getDate(year: Int, month: Int, day: Int): Date {
            CalendarUtils.getCalendar(clearToDate = true).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DATE, day)
                return time
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = CalendarUtils.getCalendar(requireArguments().getSerializableCompat(EXTRA_DATE)!!)
        return DatePickerDialog(
            requireContext(),
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DATE)
        ).apply {
            datePicker.firstDayOfWeek = requireArguments().getInt(EXTRA_WEEK_START)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        setFragmentResult(
            requestKey = DIALOG_TAG,
            result = Bundle().apply {
                putString(EXTRA_TAG, requireArguments().getString(EXTRA_TAG))
                putSerializable(EXTRA_DATE, getDate(year, month, dayOfMonth))
            }
        )
    }
}