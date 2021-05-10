package tool.xfy9326.schedule.ui.activity.module

import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.IntentUtils

class ScheduleShareModule(activity: ScheduleActivity) : AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity) {
    override fun init() {
        requireViewModel().scheduleShared.observeEvent(requireActivity()) {
            if (it == null) {
                requireViewBinding().layoutSchedule.showSnackBar(R.string.generate_share_schedule_failed)
            } else {
                requireActivity().startActivity(IntentUtils.getShareImageIntent(requireActivity(), it))
            }
        }
    }

    fun shareSchedule(weekNum: Int) {
        requireViewBinding().layoutSchedule.showSnackBar(R.string.generating_share_schedule)
        requireViewModel().shareScheduleImage(weekNum, requireActivity().resources.displayMetrics.widthPixels)
    }
}