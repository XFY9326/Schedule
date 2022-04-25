package tool.xfy9326.schedule.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import io.github.xfy9326.atools.core.castNonNull
import tool.xfy9326.schedule.utils.CalendarUtils
import java.util.*

class DatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        private val DIALOG_TAG = DatePickerDialog::class.java.simpleName

        private const val EXTRA_TAG = "EXTRA_TAG"
        private const val EXTRA_DATE = "EXTRA_DATE"
        private const val EXTRA_WEEK_START = "EXTRA_WEEK_START"

        fun showDialog(fragmentManager: FragmentManager, tag: String?, date: Date, calWeekStart: Int) {
            DatePickerDialog().apply {
                arguments = bundleOf(
                    EXTRA_TAG to tag,
                    EXTRA_WEEK_START to calWeekStart,
                    EXTRA_DATE to date
                )
            }.show(fragmentManager, tag)
        }

        fun setOnDateSetListener(fragmentManager: FragmentManager, lifecycleOwner: LifecycleOwner, block: (tag: String?, date: Date) -> Unit) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(bundle.getString(EXTRA_TAG), bundle.getSerializable(EXTRA_DATE).castNonNull())
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
        setFragmentResult(
            DIALOG_TAG, bundleOf(
                EXTRA_TAG to requireArguments().getString(EXTRA_TAG),
                EXTRA_DATE to getDate(year, month, dayOfMonth)
            )
        )
    }
}