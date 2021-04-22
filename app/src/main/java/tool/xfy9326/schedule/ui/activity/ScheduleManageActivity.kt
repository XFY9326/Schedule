package tool.xfy9326.schedule.ui.activity

import lib.xfy9326.livedata.observeNotify
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.ScheduleWrapper
import tool.xfy9326.schedule.databinding.ActivityScheduleManageBinding
import tool.xfy9326.schedule.kt.showShortSnackBar
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.ScheduleManageAdapter
import tool.xfy9326.schedule.ui.adapter.ScheduleManageAdapter.ScheduleOperation.*
import tool.xfy9326.schedule.ui.vm.ScheduleManageViewModel

class ScheduleManageActivity : ViewModelActivity<ScheduleManageViewModel, ActivityScheduleManageBinding>() {
    override val vmClass = ScheduleManageViewModel::class

    private lateinit var scheduleManageAdapter: ScheduleManageAdapter

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
            viewBinding.layoutScheduleManage.showShortSnackBar(R.string.current_schedule_set_success)
        }
    }

    override fun onInitView(viewBinding: ActivityScheduleManageBinding, viewModel: ScheduleManageViewModel) {
        viewBinding.fabAddSchedule.setOnClickListener {
            startActivity<ScheduleEditActivity>()
        }
    }

    private fun onScheduleOperate(scheduleWrapper: ScheduleWrapper, operation: ScheduleManageAdapter.ScheduleOperation) {
        when (operation) {
            COURSE_EDIT -> startActivity<CourseManageActivity> {
                putExtra(CourseManageActivity.EXTRA_SCHEDULE_ID, scheduleWrapper.schedule.scheduleId)
            }
            EDIT -> startActivity<ScheduleEditActivity> {
                putExtra(ScheduleEditActivity.INTENT_EXTRA_SCHEDULE_ID, scheduleWrapper.schedule.scheduleId)
                putExtra(ScheduleEditActivity.INTENT_EXTRA_IS_CURRENT_SCHEDULE, scheduleWrapper.inUsing)
            }
            SET_AS_CURRENT -> {
                if (scheduleWrapper.inUsing) {
                    requireViewBinding().layoutScheduleManage.showShortSnackBar(R.string.schedule_has_set_as_current)
                } else {
                    requireViewModel().setCurrentSchedule(scheduleWrapper.schedule.scheduleId)
                }
            }
        }
    }
}