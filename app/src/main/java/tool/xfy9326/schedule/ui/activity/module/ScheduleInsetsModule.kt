package tool.xfy9326.schedule.ui.activity.module

import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
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
            requireViewBinding().layoutScheduleContent.apply {
                if (layoutParams == null || layoutParams !is ViewGroup.MarginLayoutParams) {
                    updatePadding(top = systemBarInset.top)
                } else {
                    updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        updateMargins(top = systemBarInset.top)
                    }
                }
            }
            WindowInsetsCompat.Builder(insets)
                .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(systemBarInset.left, 0, systemBarInset.right, 0))
                .build()
        }
    }
}