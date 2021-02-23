package tool.xfy9326.schedule.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.Window
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.view.setMargins
import androidx.fragment.app.FragmentManager
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import tool.xfy9326.schedule.databinding.DialogCourseTimeEditBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.view.CircleNumberButton
import tool.xfy9326.schedule.utils.CourseUtils
import kotlin.properties.Delegates

class CourseTimeEditDialog : AppCompatDialogFragment() {
    companion object {
        private const val EXTRA_EDIT_POSITION = "EXTRA_EDIT_POSITION"
        private const val EXTRA_COURSE_TIME = "EXTRA_COURSE_TIME"
        private const val EXTRA_MAX_WEEK_NUM = "EXTRA_MAX_WEEK_NUM"
        private const val EXTRA_MAX_COURSE_NUM = "EXTRA_MAX_COURSE_NUM"

        private const val WINDOW_WIDTH_PERCENT = 1.0
        private const val DEFAULT_BUTTON_COUNT_IN_ROW = 4

        fun showDialog(fragmentManager: FragmentManager, editBundle: EditBundle) {
            CourseTimeEditDialog().apply {
                arguments = bundleOf(
                    EXTRA_MAX_WEEK_NUM to editBundle.maxWeekNum,
                    EXTRA_MAX_COURSE_NUM to editBundle.maxCourseNum,
                    EXTRA_COURSE_TIME to editBundle.courseTime,
                    EXTRA_EDIT_POSITION to (editBundle.editPosition ?: -1)
                )
            }.show(fragmentManager, null)
        }
    }

    class EditBundle(
        val maxWeekNum: Int,
        val maxCourseNum: Int,
        val courseTime: CourseTime?,
        val editPosition: Int?,
    )

    private var viewBinding: DialogCourseTimeEditBinding? = null
    private lateinit var editCourseTime: CourseTime
    private var maxWeekNum by Delegates.notNull<Int>()
    private var maxCourseNum by Delegates.notNull<Int>()
    private val weekNumButtonViews = ArrayList<CircleNumberButton>()
    private var weekNumCellSize by Delegates.notNull<Int>()
    private var weekNumCellMargin by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maxWeekNum = requireArguments().getInt(EXTRA_MAX_WEEK_NUM)
        maxCourseNum = requireArguments().getInt(EXTRA_MAX_COURSE_NUM)
        editCourseTime = (requireArguments().getSerializable(EXTRA_COURSE_TIME) as? CourseTime?) ?: CourseUtils.createNewCourseTime(maxWeekNum)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateEditData()
        arguments?.putSerializable(EXTRA_COURSE_TIME, editCourseTime)
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        weekNumCellSize = resources.getDimensionPixelSize(R.dimen.circle_number_button_size)
        weekNumCellMargin = resources.getDimensionPixelSize(R.dimen.circle_number_button_margin)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.course_time_edit)
            setView(DialogCourseTimeEditBinding.inflate(layoutInflater).apply {
                viewBinding = this

                buildWeekNumGrid(this)
                buildWeekNumModeButton(this)
                buildCourseTimeWheel(this)
                editTextCourseLocation.setText(editCourseTime.location.orEmpty())

                gridLayoutCourseWeeks.addOnMeasureChangedListener {
                    val count = gridLayoutCourseWeeks.measuredWidth / (weekNumCellSize + weekNumCellMargin * 2)
                    val newColumnCount = when {
                        count <= 1 -> 1
                        count.isOdd() -> count - 1
                        else -> count
                    }
                    if (newColumnCount != gridLayoutCourseWeeks.columnCount) {
                        gridLayoutCourseWeeks.columnCount = newColumnCount
                    }
                }
            }.root)

            setPositiveButton(android.R.string.ok) { _, _ ->
                viewBinding?.apply {
                    requireContext().hideKeyboard(root.windowToken)
                }
                updateEditData()
                val position = requireArguments().getInt(EXTRA_EDIT_POSITION, -1)
                requireOwner<OnCourseTimeEditListener>()?.onCourseTimeEditComplete(editCourseTime, if (position < 0) null else position)
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                viewBinding?.apply {
                    requireContext().hideKeyboard(root.windowToken)
                }
            }
        }.create().also { dialog ->
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

    override fun onStart() {
        super.onStart()
        requireDialog().setWindowWidthPercent(WINDOW_WIDTH_PERCENT)
    }

    private fun updateEditData() {
        viewBinding?.apply {
            editCourseTime.location = editTextCourseLocation.text.getText()
            editCourseTime.classTime.weekDay = WeekDay.from(pickerCourseTimeWeekDay.value)
            editCourseTime.classTime.classStartTime = pickerCourseStartTime.value
            editCourseTime.classTime.classDuration = pickerCourseEndTime.value - pickerCourseStartTime.value + 1
            editCourseTime.weekNum = weekNumButtonViews.map {
                it.isChecked
            }.toBooleanArray().arrangeWeekNum()
        }
    }

    private fun buildWeekNumModeButton(viewBinding: DialogCourseTimeEditBinding) {
        viewBinding.buttonOddWeeksMode.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            for (v in weekNumButtonViews) v.isChecked = v.showNum.isOdd()
        }
        viewBinding.buttonEvenWeeksMode.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            for (v in weekNumButtonViews) v.isChecked = v.showNum.isEven()
        }
        viewBinding.buttonOppositeWeeks.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            for (v in weekNumButtonViews) v.isChecked = !v.isChecked
        }
    }

    private fun buildWeekNumGrid(viewBinding: DialogCourseTimeEditBinding) {
        weekNumButtonViews.clear()
        viewBinding.gridLayoutCourseWeeks.apply {
            columnCount = DEFAULT_BUTTON_COUNT_IN_ROW

            if (childCount > 0) removeAllViewsInLayout()

            for (i in 1..maxWeekNum) {
                addView(createGridCell(i, weekNumCellSize, weekNumCellMargin, editCourseTime.hasThisWeekCourse(i)))
            }
        }
    }

    private fun createGridCell(num: Int, size: Int, margin: Int, checked: Boolean) =
        FrameLayout(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            }
            addView(CircleNumberButton(requireContext()).apply {
                showNum = num
                isChecked = checked
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    this.gravity = Gravity.CENTER
                    setMargins(margin)
                }
            }.also {
                weekNumButtonViews.add(it)
            })
        }

    private fun buildCourseTimeWheel(viewBinding: DialogCourseTimeEditBinding) {
        val weekDayStrArr = requireContext().getStringArray(R.array.weekday).map {
            getString(R.string.weekday, it)
        }.toTypedArray()
        val courseNumStrArr = Array(maxCourseNum) {
            getString(R.string.course_list_num_simple, it + 1)
        }
        viewBinding.pickerCourseTimeWeekDay.apply {
            minValue = 0
            maxValue = weekDayStrArr.lastIndex
            displayedValues = weekDayStrArr
            value = editCourseTime.classTime.weekDay.ordinal
        }
        viewBinding.pickerCourseStartTime.apply {
            minValue = 1
            maxValue = maxCourseNum
            displayedValues = courseNumStrArr
            value = editCourseTime.classTime.classStartTime
        }
        viewBinding.pickerCourseEndTime.apply {
            minValue = 1
            maxValue = maxCourseNum
            displayedValues = courseNumStrArr
            value = editCourseTime.classTime.classEndTime
        }
        viewBinding.pickerCourseStartTime.setOnValueChangedListener { _, _, newVal ->
            if (newVal > viewBinding.pickerCourseEndTime.value) viewBinding.pickerCourseEndTime.value = newVal
        }
        viewBinding.pickerCourseEndTime.setOnValueChangedListener { _, _, newVal ->
            if (newVal < viewBinding.pickerCourseStartTime.value) viewBinding.pickerCourseStartTime.value = newVal
        }
    }

    interface OnCourseTimeEditListener {
        fun onCourseTimeEditComplete(courseTime: CourseTime, position: Int?)
    }
}