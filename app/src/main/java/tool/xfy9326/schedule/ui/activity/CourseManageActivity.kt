package tool.xfy9326.schedule.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.livedata.observeNotify
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.databinding.ActivityCourseManageBinding
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.CourseManageAdapter
import tool.xfy9326.schedule.ui.vm.CourseManageViewModel
import tool.xfy9326.schedule.utils.consumeSystemBarInsets
import tool.xfy9326.schedule.utils.showSnackBar
import kotlin.properties.Delegates

class CourseManageActivity : ViewModelActivity<CourseManageViewModel, ActivityCourseManageBinding>() {
    companion object {
        const val EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
    }

    override val vmClass = CourseManageViewModel::class

    private var currentEditCourseScheduleId by Delegates.notNull<Long>()
    private lateinit var courseManageAdapter: CourseManageAdapter

    override fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: CourseManageViewModel) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onCreateViewBinding() = ActivityCourseManageBinding.inflate(layoutInflater)

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
        viewModel.requestDBCourses(currentEditCourseScheduleId)
        viewModel.coursesLivaData.observe(this) {
            courseManageAdapter.submitList(it.toList())
            viewBinding.recyclerViewCourseManageList.setOnlyOneAdapter(courseManageAdapter)
        }
        viewModel.courseRecovered.observeNotify(this) {
            viewBinding.layoutCourseManage.showSnackBar(R.string.recovered_success)
        }
    }

    override fun onInitView(viewBinding: ActivityCourseManageBinding, viewModel: CourseManageViewModel) {
        viewBinding.fabAddCourse.setOnSingleClickListener {
            startActivity<CourseEditActivity> {
                putExtra(CourseEditActivity.INTENT_EXTRA_SCHEDULE_ID, currentEditCourseScheduleId)
            }
        }
        viewBinding.layoutCourseAppBar.consumeSystemBarInsets(top = true)
        viewBinding.layoutCourseManageContent.consumeSystemBarInsets(bottom = true)
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