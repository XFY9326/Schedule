package tool.xfy9326.schedule.ui.activity.module

import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.dialog.ScheduleImageDialog
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class ScheduleShareModule(activity: ScheduleActivity) : AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity) {
    override fun onInit() {
        requireViewModel().scheduleShared.observeEvent(requireActivity()) {
            if (it == null) {
                requireViewBinding().layoutSchedule.showSnackBar(R.string.generate_share_schedule_failed)
            } else {
                requireActivity().startActivity(IntentUtils.getShareImageIntent(requireActivity(), it))
            }
        }
        requireViewModel().scheduleImageSaved.observeEvent(requireActivity()) {
            if (it == null) {
                requireViewBinding().layoutSchedule.showSnackBar(R.string.generate_save_schedule_failed)
            } else {
                ViewUtils.showScheduleImageSaveSnackBar(requireViewBinding().layoutSchedule, it)
            }
        }
        ScheduleImageDialog.setScheduleImageListener(requireActivity(), requireActivity().supportFragmentManager, ::generateScheduleImage)
    }

    fun shareSchedule(weekNum: Int) {
        ScheduleImageDialog.showDialog(requireActivity().supportFragmentManager, weekNum)
    }

    private fun generateScheduleImage(saveImage: Boolean, weekNum: Int) {
        requireViewModel().shareScheduleImage(saveImage, weekNum, requireActivity().resources.displayMetrics.widthPixels)
    }
}