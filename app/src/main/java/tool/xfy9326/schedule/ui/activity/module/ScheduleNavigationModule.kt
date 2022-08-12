package tool.xfy9326.schedule.ui.activity.module

import android.view.HapticFeedbackConstants
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import io.github.xfy9326.atools.core.startActivity
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.databinding.LayoutNavHeaderBinding
import tool.xfy9326.schedule.ui.activity.OnlineCourseImportActivity
import tool.xfy9326.schedule.ui.activity.ScheduleActivity
import tool.xfy9326.schedule.ui.activity.ScheduleManageActivity
import tool.xfy9326.schedule.ui.activity.SettingsActivity
import tool.xfy9326.schedule.ui.activity.base.AbstractViewModelActivityModule
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.view.NightModeViewUtils

class ScheduleNavigationModule(activity: ScheduleActivity, private val icsExportModule: ICSExportModule, private val calendarSyncModule: CalendarSyncModule) :
    AbstractViewModelActivityModule<ScheduleViewModel, ActivityScheduleBinding, ScheduleActivity>(activity), NavigationView.OnNavigationItemSelectedListener {
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    val drawerToggle
        get() = actionBarDrawerToggle

    override fun onInit() {
        requireViewModel().onlineCourseImportEnabled.observe(requireActivity()) {
            requireViewBinding().navSchedule.menu.findItem(R.id.menu_navOnlineCourseImport)?.isVisible = it
        }
        setupDrawer(requireViewBinding(), requireViewModel())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var delayCloseDrawer = true
        when (item.itemId) {
            R.id.menu_navOnlineCourseImport -> requireActivity().startActivity<OnlineCourseImportActivity>()
            R.id.menu_navCourseExportICS -> {
                icsExportModule.requestExport()
                delayCloseDrawer = false
            }
            R.id.menu_navSyncToCalendar -> {
                calendarSyncModule.syncCalendar()
                delayCloseDrawer = false
            }
            R.id.menu_navScheduleManage -> requireActivity().startActivity<ScheduleManageActivity>()
            R.id.menu_navCourseManage -> requireViewModel().openCurrentScheduleCourseManageActivity()
            R.id.menu_navSettings -> requireActivity().startActivity<SettingsActivity>()
            R.id.menu_navExit -> {
                requireActivity().finishAndRemoveTask()
                return true
            }
        }
        launch {
            if (delayCloseDrawer) delay(requireActivity().resources.getInteger(R.integer.drawer_close_anim_time).toLong())
            requireViewBinding().drawerSchedule.closeDrawers()
        }
        return true
    }

    fun onBackPressed(): Boolean {
        if (requireViewBinding().drawerSchedule.isDrawerOpen(GravityCompat.START)) {
            requireViewBinding().drawerSchedule.closeDrawers()
            return true
        }
        return false
    }

    private fun setupDrawer(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        viewBinding.navSchedule.apply {
            setNavigationItemSelectedListener(requireActivity())
            getChildAt(0)?.isVerticalScrollBarEnabled = false
            LayoutNavHeaderBinding.bind(getHeaderView(0)).buttonNightModeChange.setOnSingleClickListener {
                if (it != null && requireViewModel().nightModeChanging.compareAndSet(false, true)) {
                    it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    NightModeViewUtils.requestNightModeChange(requireActivity(), viewBinding, viewModel, it)
                }
            }
        }
        actionBarDrawerToggle =
            ActionBarDrawerToggle(requireActivity(), viewBinding.drawerSchedule, viewBinding.toolBarSchedule, R.string.open_drawer, R.string.close_drawer).apply {
                drawerArrowDrawable.color = requireActivity().getColorCompat(R.color.dark_icon)
                syncState()
                viewBinding.drawerSchedule.addDrawerListener(this)
            }
    }
}