package tool.xfy9326.schedule.ui.activity

import android.graphics.Color
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.databinding.ActivityCourseManageBinding
import tool.xfy9326.schedule.kt.observeNotify
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.CourseManageAdapter
import tool.xfy9326.schedule.ui.recyclerview.AdvancedDividerItemDecoration
import tool.xfy9326.schedule.ui.vm.CourseManageViewModel
import kotlin.properties.Delegates

class CourseManageActivity : ViewModelActivity<CourseManageViewModel, ActivityCourseManageBinding>() {
    companion object {
        const val EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
    }

    private var currentEditCourseScheduleId by Delegates.notNull<Long>()
    private lateinit var courseManageAdapter: CourseManageAdapter

    override fun onPrepare(viewBinding: ActivityCourseManageBinding, viewModel: CourseManageViewModel) {
        setSupportActionBar(viewBinding.toolBarCourseManage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currentEditCourseScheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, 0)
        if (currentEditCourseScheduleId == 0L) finish()

        courseManageAdapter = CourseManageAdapter()
        courseManageAdapter.setOnCourseEditListener(::onCourseEdit)
        courseManageAdapter.setOnCourseSwipedListener(::onCourseDeleted)
    }

    override fun onBindLiveData(viewBinding: ActivityCourseManageBinding, viewModel: CourseManageViewModel) {
        viewModel.requestDBCourses(currentEditCourseScheduleId).observe(this) {
            courseManageAdapter.submitList(it.toList())
            viewBinding.recyclerViewCourseManageList.setOnlyOneAdapter(courseManageAdapter)
        }
        viewModel.courseRecovered.observeNotify(this) {
            viewBinding.layoutCourseManage.showShortSnackBar(R.string.recovered_success)
        }
    }

    override fun onInitView(viewBinding: ActivityCourseManageBinding, viewModel: CourseManageViewModel) {
        viewBinding.recyclerViewCourseManageList.addItemDecoration(
            AdvancedDividerItemDecoration(this@CourseManageActivity, DividerItemDecoration.VERTICAL)
        )
        viewBinding.fabAddCourse.setOnClickListener {
            startActivity<CourseEditActivity> {
                putExtra(CourseEditActivity.INTENT_EXTRA_SCHEDULE_ID, currentEditCourseScheduleId)
            }
        }
    }

    private fun onCourseEdit(course: Course) {
        startActivity<CourseEditActivity> {
            putExtra(CourseEditActivity.INTENT_EXTRA_COURSE_ID, course.courseId)
            putExtra(CourseEditActivity.INTENT_EXTRA_SCHEDULE_ID, currentEditCourseScheduleId)
        }
    }

    private fun onCourseDeleted(course: Course) {
        requireViewModel().deleteCourse(course)
        Snackbar.make(requireViewBinding().layoutCourseManage, getString(R.string.course_delete_success, course.name), Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.RED)
            .setAction(R.string.recover) {
                requireViewModel().recoverCourse(course)
            }.show()
    }
}