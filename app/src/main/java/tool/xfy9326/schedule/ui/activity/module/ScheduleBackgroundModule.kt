package tool.xfy9326.schedule.ui.activity.module

import android.widget.ImageView
import coil.load
import tool.xfy9326.schedule.beans.ImageScaleType
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import kotlin.math.roundToInt

class ScheduleBackgroundModule(activity: ScheduleActivity) : AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity) {
    companion object {
        fun setBackgroundView(imageView: ImageView, bundle: ScheduleDataStore.ScheduleBackgroundBuildBundle?) {
            imageView.apply {
                if (bundle == null) {
                    setImageDrawable(null)
                } else {
                    scaleType = when (bundle.scaleType) {
                        ImageScaleType.FIT_CENTER -> ImageView.ScaleType.FIT_CENTER
                        ImageScaleType.CENTER_CROP -> ImageView.ScaleType.CENTER_CROP
                        ImageScaleType.CENTER_INSIDE -> ImageView.ScaleType.CENTER_INSIDE
                    }
                    load(bundle.file) {
                        if (bundle.useAnim) crossfade(true)
                        imageAlpha = (bundle.alpha * 255).roundToInt()
                    }
                }
            }
        }
    }

    override fun onInit() {
        requireViewModel().scheduleBackground.observe(requireActivity()) {
            setBackgroundView(requireViewBinding().imageViewScheduleBackground, it)
        }
    }
}