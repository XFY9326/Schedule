package tool.xfy9326.schedule.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.kt.requireOwner
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

class DatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_DATE = "EXTRA_DATE"
        private const val EXTRA_WEEK_START = "EXTRA_WEEK_START"

        fun showDialog(fragmentManager: FragmentManager, tag: String?, date: Date, calWeekStart: Int) {
            DatePickerDialog().apply {
                arguments = buildBundle {
                    putString(EXTRA_TAG, tag)
                    putInt(EXTRA_WEEK_START, calWeekStart)
                    putSerializable(EXTRA_DATE, date)
                }
            }.show(fragmentManager, tag)
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
        val calendar = CalendarUtils.getCalendar(requireArguments().getSerializable(EXTRA_DATE) as Date)
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
        requireOwner<OnDateSetListener>()?.onDateSet(requireArguments().getString(EXTRA_TAG), getDate(year, month, dayOfMonth))
    }

    interface OnDateSetListener {
        fun onDateSet(tag: String?, date: Date)
    }
}