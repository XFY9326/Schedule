package tool.xfy9326.schedule.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import tool.xfy9326.schedule.kt.buildBundle
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

class DatePickerDialog : DialogFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_DATE = "EXTRA_DATE"
        private const val EXTRA_FIRST_DAY_OF_WEEK = "EXTRA_FIRST_DAY_OF_WEEK"

        fun showDialog(fragmentManager: FragmentManager, tag: String?, date: Date, firstDayOfWeek: Int) {
            DatePickerDialog().apply {
                arguments = buildBundle {
                    putString(EXTRA_TAG, tag)
                    putInt(EXTRA_FIRST_DAY_OF_WEEK, firstDayOfWeek)
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
            datePicker.firstDayOfWeek = requireArguments().getInt(EXTRA_FIRST_DAY_OF_WEEK)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val owner = requireContext()
        if (owner is OnDateSetListener) owner.onDateSet(requireArguments().getString(EXTRA_TAG), getDate(year, month, dayOfMonth))
    }

    interface OnDateSetListener {
        fun onDateSet(tag: String?, date: Date)
    }
}