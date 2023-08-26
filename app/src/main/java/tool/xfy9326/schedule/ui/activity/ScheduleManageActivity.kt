package tool.xfy9326.schedule.ui.activity

import android.os.Bundle
import androidx.core.view.WindowCompat
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.livedata.observeNotify
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleWrapper
import tool.xfy9326.schedule.databinding.ActivityScheduleManageBinding
import tool.xfy9326.schedule.kt.consumeSystemBarInsets
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.ScheduleManageAdapter
import tool.xfy9326.schedule.ui.adapter.ScheduleManageAdapter.ScheduleOperation.COURSE_EDIT
import tool.xfy9326.schedule.ui.adapter.ScheduleManageAdapter.ScheduleOperation.EDIT
import tool.xfy9326.schedule.ui.adapter.ScheduleManageAdapter.ScheduleOperation.SET_AS_CURRENT
import tool.xfy9326.schedule.ui.vm.ScheduleManageViewModel

class ScheduleManageActivity : ViewModelActivity<ScheduleManageViewModel, ActivityScheduleManageBinding>() {
    override val vmClass = ScheduleManageViewModel::class

    private lateinit var scheduleManageAdapter: ScheduleManageAdapter

    override fun onContentViewPreload(savedInstanceState: Bundle?, viewModel: ScheduleManageViewModel) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onCreateViewBinding() = ActivityScheduleManageBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityScheduleManageBinding, viewModel: ScheduleManageViewModel) {
        setSupportActionBar(viewBinding.toolBarScheduleManage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scheduleManageAdapter = ScheduleManageAdapter()
        scheduleManageAdapter.setOnScheduleOperateListener(::onScheduleOperate)
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleManageBinding, viewModel: ScheduleManageViewModel) {
        viewModel.schedules.observe(this) {
            scheduleManageAdapter.update(it.first, it.second)
            viewBinding.recyclerViewScheduleManageList.setOnlyOneAdapter(scheduleManageAdapter)
        }
        viewModel.setCurrentScheduleSuccess.observeNotify(this) {
            viewBinding.layoutScheduleManage.showSnackBar(R.string.current_schedule_set_success)
        }
    }

    override fun onInitView(viewBinding: ActivityScheduleManageBinding, viewModel: ScheduleManageViewModel) {
        viewBinding.fabAddSchedule.setOnSingleClickListener {
            startActivity<ScheduleEditActivity>()
        }
        viewBinding.layoutScheduleAppBar.consumeSystemBarInsets(top = true)
        viewBinding.layoutScheduleManageContent.consumeSystemBarInsets(bottom = true)
    }

    private fun onScheduleOperate(scheduleWrapper: ScheduleWrapper, operation: ScheduleManageAdapter.ScheduleOperation) {
        when (operation) {
            COURSE_EDIT -> startActivity<CourseManageActivity> {
                putExtra(CourseManageActivity.EXTRA_SCHEDULE_ID, scheduleWrapper.schedule.scheduleId)
            }

            EDIT -> ScheduleEditActivity.startActivity(this, scheduleWrapper.schedule.scheduleId, scheduleWrapper.inUsing)
            SET_AS_CURRENT -> {
                if (scheduleWrapper.inUsing) {
                    requireViewBinding().layoutScheduleManage.showSnackBar(R.string.schedule_has_set_as_current)
                } else {
                    requireViewModel().setCurrentSchedule(scheduleWrapper.schedule.scheduleId)
                }
            }
        }
    }
}