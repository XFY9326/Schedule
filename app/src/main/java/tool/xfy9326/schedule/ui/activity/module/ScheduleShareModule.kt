package tool.xfy9326.schedule.ui.activity.module

import androidx.annotation.Px
import io.github.xfy9326.atools.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.kt.showSnackBar
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.dialog.ScheduleImageDialog
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.view.ViewUtils

class ScheduleShareModule(activity: ScheduleActivity) :
    AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity) {
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
        ScheduleImageDialog.setScheduleImageListener(requireActivity(), requireActivity().supportFragmentManager) { saveImage, weekNum ->
            val panel = requireViewBinding().viewPagerSchedulePanel
            generateScheduleImage(saveImage, weekNum, panel.measuredWidth, panel.measuredHeight)
        }
    }

    fun shareSchedule(weekNum: Int) {
        ScheduleImageDialog.showDialog(requireActivity().supportFragmentManager, weekNum)
    }

    private fun generateScheduleImage(saveImage: Boolean, weekNum: Int, @Px viewWidth: Int, @Px viewHeight: Int) {
        val targetWidth = if(viewWidth == 0) requireActivity().resources.displayMetrics.widthPixels else viewWidth
        requireViewModel().shareScheduleImage(saveImage, weekNum, targetWidth, viewHeight)
    }
}