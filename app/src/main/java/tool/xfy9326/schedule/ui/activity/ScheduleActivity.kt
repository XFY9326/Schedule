package tool.xfy9326.schedule.ui.activity

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.xfy9326.android.kit.getColorCompat
import lib.xfy9326.android.kit.setIconTint
import lib.xfy9326.android.kit.setOnSingleClickListener
import lib.xfy9326.android.kit.startActivity
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.beans.WeekNumType.Companion.getText
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.databinding.LayoutNavHeaderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.activity.module.CalendarSyncModule
import tool.xfy9326.schedule.ui.activity.module.ICSExportModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleBackgroundModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleShareModule
import tool.xfy9326.schedule.ui.adapter.ScheduleViewPagerAdapter
import tool.xfy9326.schedule.ui.dialog.CourseDetailDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleControlPanel
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.*
import tool.xfy9326.schedule.utils.view.NightModeViewUtils

class ScheduleActivity : ViewModelActivity<ScheduleViewModel, ActivityScheduleBinding>(), NavigationView.OnNavigationItemSelectedListener {
    override val vmClass = ScheduleViewModel::class

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var scheduleViewPagerAdapter: ScheduleViewPagerAdapter? = null

    private val icsExportModule = ICSExportModule(this)
    private val scheduleBackgroundModule = ScheduleBackgroundModule(this)
    private val calendarSyncModule = CalendarSyncModule(this)
    private val scheduleShareModule = ScheduleShareModule(this)

    override fun onCreateViewBinding() = ActivityScheduleBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setSupportActionBar(viewBinding.toolBarSchedule)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        viewBinding.viewPagerSchedulePanel.offscreenPageLimit = 1
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        viewModel.weekNumInfo.observe(this) {
            setupViewPager(it.first, it.second)
            refreshToolBarTime(it.first)
        }
        viewModel.nowDay.observe(this, ::updateDate)
        viewModel.showWeekChanged.observeEvent(this) {
            updateShowWeekNum(it.first, it.second)
        }
        scheduleBackgroundModule.init()
        viewModel.scrollToWeek.observeEvent(this, observer = ::scrollToWeek)
        viewModel.showScheduleControlPanel.observeEvent(this) {
            ScheduleControlPanel.showDialog(supportFragmentManager, getCurrentShowWeekNum(), it.first, it.second, it.third)
        }
        viewModel.showCourseDetailDialog.observeEvent(this) {
            CourseDetailDialog.showDialog(supportFragmentManager, it)
        }
        viewModel.openCourseManageActivity.observeEvent(this) {
            startActivity<CourseManageActivity> {
                putExtra(CourseManageActivity.EXTRA_SCHEDULE_ID, it)
            }
        }
        viewModel.exitAppDirectly.observeEvent(this) {
            if (it) {
                finish()
            } else {
                moveTaskToBack(false)
            }
        }
        viewModel.toolBarTintColor.observe(this, ::setToolBarTintColor)
        viewModel.scheduleSystemBarAppearance.observe(this) {
            window.setSystemBarAppearance(it)
        }
        viewModel.onlineCourseImportEnabled.observe(this) {
            viewBinding.navSchedule.menu.findItem(R.id.menu_navOnlineCourseImport)?.isVisible = it
        }
        icsExportModule.init()
        calendarSyncModule.init()
        scheduleShareModule.init()
    }

    override fun onInitView(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setupDrawer(viewBinding, viewModel)
        NightModeViewUtils.checkNightModeChangedAnimation(this, viewBinding, viewModel)

        viewBinding.viewPagerSchedulePanel.registerOnPageChangeCallback(ScheduleViewPagerChangeCallback())

        viewBinding.layoutScheduleDateInfoBar.setOnSingleClickListener {
            viewModel.scrollToCurrentWeekNum()
        }

        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            val systemBarInset = WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(WindowInsetsCompat.Type.systemBars())
            viewBinding.layoutScheduleContent.apply {
                if (layoutParams == null || layoutParams !is ViewGroup.MarginLayoutParams) {
                    updatePadding(top = systemBarInset.top)
                } else {
                    updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        updateMargins(top = systemBarInset.top)
                    }
                }
            }
            WindowInsetsCompat.Builder(WindowInsetsCompat.toWindowInsetsCompat(insets))
                .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(systemBarInset.left, 0, systemBarInset.right, 0))
                .build()
                .toWindowInsets()
        }

        ScheduleControlPanel.addScrollToWeekListener(supportFragmentManager, this) {
            scrollToWeek(it)
        }
    }

    override fun onHandleSavedInstanceState(bundle: Bundle?, viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        if (intent?.getBooleanExtra(SplashActivity.INTENT_EXTRA_APP_INIT_LAUNCH, false) == true) {
            UpgradeUtils.checkUpgrade(this, false,
                onFoundUpgrade = { UpgradeDialog.showDialog(supportFragmentManager, it) }
            )
        }
        super.onHandleSavedInstanceState(bundle, viewBinding, viewModel)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_schedule, menu)
        requireViewModel().toolBarTintColor.value?.let {
            menu?.setIconTint(it)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_scheduleControlPanel -> requireViewModel().showScheduleControlPanel()
            R.id.menu_scheduleShare -> scheduleShareModule.shareSchedule(getCurrentShowWeekNum())
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (requireViewBinding().drawerSchedule.isDrawerOpen(GravityCompat.START)) {
            requireViewBinding().drawerSchedule.closeDrawers()
        } else {
            requireViewModel().exitAppDirectly()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var delayCloseDrawer = true
        when (item.itemId) {
            R.id.menu_navOnlineCourseImport -> startActivity<OnlineCourseImportActivity>()
            R.id.menu_navCourseExportICS -> {
                icsExportModule.requestExport()
                delayCloseDrawer = false
            }
            R.id.menu_navSyncToCalendar -> {
                calendarSyncModule.syncCalendar()
                delayCloseDrawer = false
            }
            R.id.menu_navScheduleManage -> startActivity<ScheduleManageActivity>()
            R.id.menu_navCourseManage -> requireViewModel().openCurrentScheduleCourseManageActivity()
            R.id.menu_navSettings -> startActivity<SettingsActivity>()
            R.id.menu_navExit -> {
                finishAndRemoveTask()
                return true
            }
        }
        lifecycleScope.launch {
            if (delayCloseDrawer) delay(resources.getInteger(R.integer.drawer_close_anim_time).toLong())
            requireViewBinding().drawerSchedule.closeDrawers()
        }
        return true
    }

    private fun setToolBarTintColor(color: Int?) {
        val tintColor = color ?: getColorCompat(R.color.schedule_tool_bar_tint)
        requireViewBinding().apply {
            textViewScheduleTodayDate.setTextColor(tintColor)
            textViewScheduleNotCurrentWeek.setTextColor(tintColor)
            textViewScheduleNowShowWeekNum.setTextColor(tintColor)
            invalidateOptionsMenu()
            // PorterDuffColorFilter在Android P以下显示错误
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                toolBarSchedule.navigationIcon?.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC)
            } else {
                actionBarDrawerToggle?.drawerArrowDrawable?.color = tintColor
            }
        }
    }

    private fun getCurrentShowWeekNum() = requireViewBinding().viewPagerSchedulePanel.currentItem + 1

    private fun refreshToolBarTime(nowWeekNum: Int) {
        val useWeekNum = if (requireViewModel().currentScrollPosition == null) {
            // In vacation -> Show first week
            if (nowWeekNum == 0) 1 else nowWeekNum
        } else {
            getCurrentShowWeekNum()
        }
        updateShowWeekNum(useWeekNum, WeekNumType.create(useWeekNum, nowWeekNum))
    }

    private fun setupViewPager(nowWeekNum: Int, maxWeekNum: Int) {
        requireViewBinding().viewPagerSchedulePanel.apply {
            if (adapter == null) {
                // In vacation -> Show first week
                var position = nowWeekNum
                if (nowWeekNum != 0) position--

                scheduleViewPagerAdapter = ScheduleViewPagerAdapter(this@ScheduleActivity, maxWeekNum)
                adapter = scheduleViewPagerAdapter
                setCurrentItem(requireViewModel().currentScrollPosition ?: position, false)
            } else {
                scheduleViewPagerAdapter?.updateMaxWeekNum(maxWeekNum)
            }
        }
    }

    private fun setupDrawer(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        viewBinding.navSchedule.apply {
            setNavigationItemSelectedListener(this@ScheduleActivity)
            getChildAt(0)?.isVerticalScrollBarEnabled = false
            LayoutNavHeaderBinding.bind(getHeaderView(0)).buttonNightModeChange.setOnSingleClickListener {
                if (it != null && requireViewModel().nightModeChanging.compareAndSet(false, true)) {
                    it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    NightModeViewUtils.requestNightModeChange(this@ScheduleActivity, viewBinding, viewModel, it)
                }
            }
        }
        actionBarDrawerToggle = ActionBarDrawerToggle(this, viewBinding.drawerSchedule, viewBinding.toolBarSchedule, R.string.open_drawer, R.string.close_drawer).apply {
            drawerArrowDrawable.color = getColorCompat(R.color.dark_icon)
            syncState()
            viewBinding.drawerSchedule.addDrawerListener(this)
        }
    }

    private fun scrollToWeek(weekNum: Int) {
        requireViewBinding().viewPagerSchedulePanel.currentItem = weekNum - 1
    }

    private fun updateDate(day: Day) {
        requireViewBinding().textViewScheduleTodayDate.text = getString(R.string.month_date, day.month, day.day)
    }

    private fun updateShowWeekNum(weekNum: Int, weekNumType: WeekNumType) {
        requireViewBinding().apply {
            textViewScheduleNowShowWeekNum.text = getString(R.string.week_num, weekNum)
            textViewScheduleNotCurrentWeek.text = weekNumType.getText(this@ScheduleActivity)
        }
    }

    private inner class ScheduleViewPagerChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            requireViewModel().apply {
                currentScrollPosition = position
                notifyShowWeekChanged(position + 1)
            }
        }
    }
}