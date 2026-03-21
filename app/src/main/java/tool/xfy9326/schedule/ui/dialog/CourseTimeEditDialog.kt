package tool.xfy9326.schedule.ui.dialog

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.SectionTime.Companion.end
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.databinding.DialogCourseTimeEditBinding
import tool.xfy9326.schedule.tools.core.getParcelableCompat
import tool.xfy9326.schedule.tools.core.hideKeyboard
import tool.xfy9326.schedule.tools.ui.getStringArray
import tool.xfy9326.schedule.tools.ui.getText
import tool.xfy9326.schedule.tools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.utils.consumeBottomInsets
import tool.xfy9326.schedule.utils.schedule.CourseUtils
import kotlin.properties.Delegates

class CourseTimeEditDialog : BottomSheetDialogFragment() {
    companion object {
        private val DIALOG_TAG = CourseTimeEditDialog::class.java.simpleName

        private const val EXTRA_EDIT_POSITION = "EXTRA_EDIT_POSITION"
        private const val EXTRA_COURSE_TIME = "EXTRA_COURSE_TIME"
        private const val EXTRA_MAX_WEEK_NUM = "EXTRA_MAX_WEEK_NUM"
        private const val EXTRA_MAX_COURSE_NUM = "EXTRA_MAX_COURSE_NUM"

        fun showDialog(fragmentManager: FragmentManager, editBundle: EditBundle) {
            CourseTimeEditDialog().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_MAX_WEEK_NUM, editBundle.maxWeekNum)
                    putInt(EXTRA_MAX_COURSE_NUM, editBundle.maxCourseNum)
                    putParcelable(EXTRA_COURSE_TIME, editBundle.courseTime)
                    putInt(EXTRA_EDIT_POSITION, editBundle.editPosition ?: -1)
                }
            }.show(fragmentManager, DIALOG_TAG)
        }

        fun setCourseTimeEditListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            block: (courseTime: CourseTime, position: Int?) -> Unit
        ) {
            fragmentManager.setFragmentResultListener(DIALOG_TAG, lifecycleOwner) { _, bundle ->
                block(
                    bundle.getParcelableCompat(EXTRA_COURSE_TIME)!!,
                    if (bundle.containsKey(EXTRA_EDIT_POSITION)) {
                        bundle.getInt(EXTRA_EDIT_POSITION, -1)
                    } else {
                        null
                    }
                )
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maxWeekNum = requireArguments().getInt(EXTRA_MAX_WEEK_NUM)
        maxCourseNum = requireArguments().getInt(EXTRA_MAX_COURSE_NUM)
        editCourseTime = (requireArguments().getParcelableCompat(EXTRA_COURSE_TIME)) ?: CourseUtils.createNewCourseTime(maxWeekNum)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateEditData()
        arguments?.putParcelable(EXTRA_COURSE_TIME, editCourseTime)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), R.style.AppTheme_TransparentBottomSheetDialog).apply {
            setContentView(DialogCourseTimeEditBinding.inflate(layoutInflater).apply {
                viewBinding = this

                viewWeekNumEdit.setWeekNum(editCourseTime.weekNumArray, maxWeekNum)

                buildWeekNumModeButton(this)
                buildCourseTimeWheel(this)
                editTextCourseLocation.setText(editCourseTime.location.orEmpty())

                buttonConfirm.setOnSingleClickListener {
                    viewBinding?.apply {
                        requireContext().hideKeyboard(root.windowToken)
                    }
                    updateEditData()
                    val position = requireArguments().getInt(EXTRA_EDIT_POSITION, -1)
                    val outputBundle = Bundle().apply {
                        if (position < 0) {
                            putParcelable(EXTRA_COURSE_TIME, editCourseTime)
                        } else {
                            putInt(EXTRA_EDIT_POSITION, position)
                            putParcelable(EXTRA_COURSE_TIME, editCourseTime)
                        }
                    }
                    setFragmentResult(DIALOG_TAG, outputBundle)
                    dismiss()
                }
                buttonCancel.setOnSingleClickListener {
                    viewBinding?.apply {
                        requireContext().hideKeyboard(root.windowToken)
                    }
                    dismiss()
                }
            }.root)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    override fun onStart() {
        super.onStart()
        consumeBottomInsets()
    }

    private fun updateEditData() {
        viewBinding?.apply {
            editCourseTime.location = editTextCourseLocation.text.getText()?.trim()
            editCourseTime.sectionTime.weekDay = WeekDay.from(pickerCourseTimeWeekDay.value)
            editCourseTime.sectionTime.start = pickerCourseStartTime.value
            editCourseTime.sectionTime.duration = pickerCourseEndTime.value - pickerCourseStartTime.value + 1
            editCourseTime.weekNumArray = viewWeekNumEdit.getWeekNumArray()
        }
    }

    private fun buildWeekNumModeButton(viewBinding: DialogCourseTimeEditBinding) {
        viewBinding.buttonOddWeeksMode.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewBinding.viewWeekNumEdit.checkAllOddWeekNum()

        }
        viewBinding.buttonEvenWeeksMode.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewBinding.viewWeekNumEdit.checkAllEvenWeekNum()
        }
        viewBinding.buttonOppositeWeeks.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewBinding.viewWeekNumEdit.checkAllOppositeWeekNum()
        }
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
            value = editCourseTime.sectionTime.weekDay.ordinal
        }
        viewBinding.pickerCourseStartTime.apply {
            minValue = 1
            maxValue = maxCourseNum
            displayedValues = courseNumStrArr
            value = editCourseTime.sectionTime.start
        }
        viewBinding.pickerCourseEndTime.apply {
            minValue = 1
            maxValue = maxCourseNum
            displayedValues = courseNumStrArr
            value = editCourseTime.sectionTime.end
        }
        viewBinding.pickerCourseStartTime.setOnValueChangedListener { _, _, newVal ->
            if (newVal > viewBinding.pickerCourseEndTime.value) viewBinding.pickerCourseEndTime.value = newVal
        }
        viewBinding.pickerCourseEndTime.setOnValueChangedListener { _, _, newVal ->
            if (newVal < viewBinding.pickerCourseStartTime.value) viewBinding.pickerCourseStartTime.value = newVal
        }
    }
}