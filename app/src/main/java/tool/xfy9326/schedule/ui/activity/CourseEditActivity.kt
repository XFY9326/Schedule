package tool.xfy9326.schedule.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import io.github.xfy9326.atools.core.hideKeyboard
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.livedata.observeNotify
import io.github.xfy9326.atools.ui.getText
import io.github.xfy9326.atools.ui.resume
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.EditError.Companion.getText
import tool.xfy9326.schedule.databinding.ActivityCourseEditBinding
import tool.xfy9326.schedule.kt.consumeSystemBarInsets
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.CourseTimeAdapter
import tool.xfy9326.schedule.ui.dialog.CourseTimeEditDialog
import tool.xfy9326.schedule.ui.vm.CourseEditViewModel
import tool.xfy9326.schedule.utils.view.DialogUtils
import kotlin.properties.Delegates

class CourseEditActivity : ViewModelActivity<CourseEditViewModel, ActivityCourseEditBinding>(), ColorPickerDialogListener {
    companion object {
        const val INTENT_EXTRA_COURSE_ID = "EXTRA_COURSE_ID"
        const val INTENT_EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
    }

    override val vmClass = CourseEditViewModel::class

    private var currentEditScheduleId by Delegates.notNull<Long>()
    private lateinit var courseTimeAdapter: CourseTimeAdapter

    override fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: CourseEditViewModel) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        onBackPressedDispatcher.addCallback(this, true, this::onBackPressed)
    }

    override fun onCreateViewBinding() = ActivityCourseEditBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityCourseEditBinding, viewModel: CourseEditViewModel) {
        setSupportActionBar(viewBinding.toolBarCourseEdit.toolBarGeneral)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currentEditScheduleId = intent.getLongExtra(INTENT_EXTRA_SCHEDULE_ID, 0)
        if (currentEditScheduleId == 0L) finish()

        courseTimeAdapter = CourseTimeAdapter()

        viewModel.requestDBCourse(currentEditScheduleId, intent.getLongExtra(INTENT_EXTRA_COURSE_ID, 0))
    }

    override fun onBindLiveData(viewBinding: ActivityCourseEditBinding, viewModel: CourseEditViewModel) {
        viewModel.courseData.observe(this, ::applyCourseToView)
        viewModel.courseSaveComplete.observeEvent(this, observer = ::onCourseSaved)
        viewModel.courseSaveEmptyWeekNum.observeNotify(this) {
            Snackbar.make(requireViewBinding().layoutCourseEdit, R.string.save_empty_week_num_course_time, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.RED)
                .setAction(android.R.string.ok) {
                    viewModel.saveCourse(currentEditScheduleId, false)
                }.show()
        }
        viewModel.courseSaveFailed.observeEvent(this) {
            viewBinding.layoutCourseEdit.showSnackBar(it.getText(this))
        }
        viewModel.copyToOtherSchedule.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutCourseEdit.showSnackBar(R.string.course_copy_success)
            } else {
                viewBinding.layoutCourseEdit.showSnackBar(R.string.course_copy_failed, it.getText(this))
            }
        }
        viewModel.loadAllSchedules.observeEvent(this) {
            if (it.isEmpty()) {
                viewBinding.layoutCourseEdit.showSnackBar(R.string.empty_schedule_list)
            } else {
                DialogUtils.showScheduleSelectDialog(this, R.string.copy_to_other_schedule, it) { _, id ->
                    viewModel.copyToOtherSchedule(id)
                }
            }
        }
        viewModel.editCourseTime.observeEvent(this) {
            CourseTimeEditDialog.showDialog(supportFragmentManager, it)
        }
    }

    override fun onInitView(viewBinding: ActivityCourseEditBinding, viewModel: CourseEditViewModel) {
        courseTimeAdapter.setOnCourseTimeEditListener(::editCourseTime)
        courseTimeAdapter.setOnCourseTimeDeleteListener(::deleteCourseTime)
        viewBinding.fabAddCourseTime.setOnSingleClickListener {
            viewModel.editCourseTime(currentEditScheduleId)
        }
        viewBinding.buttonCourseColorEdit.setOnSingleClickListener {
            DialogUtils.showColorPickerDialog(this, R.string.course_color_edit, viewModel.editCourse.color)
        }
        CourseTimeEditDialog.setCourseTimeEditListener(supportFragmentManager, this, ::onCourseTimeAddOrUpdate)
        viewBinding.layoutCourseEditContent.consumeSystemBarInsets(bottom = true)
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        requireViewModel().apply {
            if (editCourse.color != color) {
                editCourse.color = color
                updateCourseColor(color)
            }
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_course_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_courseEditSave -> {
                requireViewModel().apply {
                    requireViewBinding().layoutCourseEdit.apply {
                        clearFocus()
                        hideKeyboard(windowToken)
                    }
                    updateTextData()
                    saveCourse(currentEditScheduleId)
                }
            }

            R.id.menu_courseEditCopy -> requireViewModel().loadAllSchedules(currentEditScheduleId)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateTextData()
        super.onSaveInstanceState(outState)
    }

    private fun onBackPressed(callback: OnBackPressedCallback) {
        updateTextData()
        if (requireViewModel().hasDataChanged()) {
            Snackbar.make(requireViewBinding().layoutCourseEdit, R.string.ask_whether_exit_without_save, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.RED)
                .setAction(android.R.string.ok) {
                    callback.resume(onBackPressedDispatcher)
                }.show()
        } else {
            callback.resume(onBackPressedDispatcher)
        }
    }

    private fun onCourseTimeAddOrUpdate(courseTime: CourseTime, position: Int?) {
        requireViewModel().editCourse.apply {
            if (times.isEmpty() && (position == null || position < 0)) {
                times = listOf(courseTime)
                courseTimeAdapter.submitList(times)
            } else {
                val newList = times.toMutableList()
                if (position == null || position < 0) {
                    newList.add(courseTime)
                    times = newList
                } else {
                    newList[position] = courseTime
                }
                times = newList
                courseTimeAdapter.submitList(newList)
            }
        }
    }

    private fun updateTextData() {
        requireViewModel().apply {
            editCourse.name = requireViewBinding().editTextCourseName.text?.toString()?.trim().orEmpty()
            editCourse.teacher = requireViewBinding().editTextCourseTeacherName.text.getText()?.trim()
        }
    }

    private fun applyCourseToView(course: Course) {
        requireViewBinding().apply {
            courseTimeAdapter.submitList(course.times)
            recyclerViewCourseTimeList.setOnlyOneAdapter(courseTimeAdapter)

            editTextCourseName.setText(course.name)
            editTextCourseTeacherName.setText(course.teacher.orEmpty())
            updateCourseColor(course.color)
        }
    }

    private fun updateCourseColor(color: Int) {
        requireViewBinding().buttonCourseColorEdit.imageTintList = ColorStateList.valueOf(color)
    }

    private fun editCourseTime(position: Int, courseTime: CourseTime) =
        requireViewModel().editCourseTime(currentEditScheduleId, courseTime, position)

    private fun deleteCourseTime(position: Int, courseTime: CourseTime) {
        requireViewModel().editCourse.apply {
            val newList = times.toMutableList()
            newList.removeAt(position)
            times = newList
            courseTimeAdapter.submitList(newList)
        }

        Snackbar.make(requireViewBinding().layoutCourseEdit, R.string.course_time_delete_success, Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.RED)
            .setAction(R.string.recover) {
                requireViewModel().editCourse.apply {
                    val newList = times.toMutableList()
                    newList.add(position, courseTime)
                    times = newList
                    courseTimeAdapter.submitList(newList)
                }
                requireViewBinding().layoutCourseEdit.showSnackBar(R.string.recovered_success)
            }.show()
    }

    private fun onCourseSaved(newCourseId: Long) {
        intent.putExtra(INTENT_EXTRA_COURSE_ID, newCourseId)
        requireViewBinding().layoutCourseEdit.showSnackBar(R.string.save_success)
    }
}