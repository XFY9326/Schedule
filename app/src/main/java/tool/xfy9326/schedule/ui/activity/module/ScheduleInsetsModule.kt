package tool.xfy9326.schedule.ui.activity.module

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel

class ScheduleInsetsModule(activity: ScheduleActivity) :
    AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity) {
    override fun onInit() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        ViewCompat.setOnApplyWindowInsetsListener(requireActivity().window.decorView) { _, insets ->
            val systemBarInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            requireViewBinding().layoutScheduleContent.updatePadding(
                top = systemBarInset.top,
                left = systemBarInset.left + cutoutInsets.left,
                right = systemBarInset.right + cutoutInsets.right
            )
            WindowInsetsCompat.Builder(insets)
                .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(systemBarInset.left, 0, systemBarInset.right, systemBarInset.bottom))
                .build()
        }
        ViewCompat.setOnApplyWindowInsetsListener(requireViewBinding().navSchedule) { v, insets ->
            val systemBarInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInset = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = systemBarInset.left + cutoutInset.left)
            WindowInsetsCompat.CONSUMED
        }
    }
}