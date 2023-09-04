package tool.xfy9326.schedule.ui.activity

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import io.github.xfy9326.atools.livedata.observeEvent
import io.github.xfy9326.atools.livedata.observeNotify
import io.github.xfy9326.atools.ui.getColorCompat
import io.github.xfy9326.atools.ui.setIconTint
import io.github.xfy9326.atools.ui.setOnSingleClickListener
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.beans.WeekNumType.Companion.getText
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.activity.module.CalendarSyncModule
import tool.xfy9326.schedule.ui.activity.module.ICSExportModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleBackgroundModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleInsetsModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleLaunchModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleNavigationModule
import tool.xfy9326.schedule.ui.activity.module.ScheduleShareModule
import tool.xfy9326.schedule.ui.adapter.ScheduleViewPagerAdapter
import tool.xfy9326.schedule.ui.dialog.CourseDetailDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleControlPanel
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.setSystemBarAppearance
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.NightModeViewUtils

class ScheduleActivity : ViewModelActivity<ScheduleViewModel, ActivityScheduleBinding>(), NavigationView.OnNavigationItemSelectedListener {
    override val vmClass = ScheduleViewModel::class

    private var scheduleViewPagerAdapter: ScheduleViewPagerAdapter? = null

    private val scheduleLaunchModule = ScheduleLaunchModule(this)
    private val icsExportModule = ICSExportModule(this)
    private val scheduleBackgroundModule = ScheduleBackgroundModule(this)
    private val calendarSyncModule = CalendarSyncModule(this)
    private val scheduleShareModule = ScheduleShareModule(this)
    private val scheduleInsetsModule = ScheduleInsetsModule(this)
    private val scheduleNavigationModule = ScheduleNavigationModule(this, icsExportModule, calendarSyncModule)

    override fun onCreateViewBinding() = ActivityScheduleBinding.inflate(layoutInflater)

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        scheduleLaunchModule.init()
    }

    override fun onValidateLaunch(savedInstanceState: Bundle?): Boolean {
        return savedInstanceState != null || ScheduleLaunchModule.checkDoAppErrorLaunch(this)
    }

    override fun onPrepare(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setSupportActionBar(viewBinding.toolBarSchedule)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        viewBinding.viewPagerSchedulePanel.offscreenPageLimit = 1
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        viewModel.scheduleAlert.observe(this) {
            invalidateOptionsMenu()
        }
        viewModel.scheduleAlertDialog.observeNotify(this) {
            DialogUtils.showEmptyWeekNumCourseAlertDialog(this, this) {
                requireViewModel().openCurrentScheduleCourseManageActivity()
            }
        }
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
            ScheduleControlPanel.showDialog(supportFragmentManager, getCurrentShowWeekNum(), it.first, it.second)
        }
        viewModel.showCourseDetailDialog.observeEvent(this) {
            CourseDetailDialog.showDialog(supportFragmentManager, it)
        }
        viewModel.toolBarTintColor.observe(this, ::setToolBarTintColor)
        viewModel.scheduleSystemBarAppearance.observe(this) {
            window.setSystemBarAppearance(it)
        }
        icsExportModule.init()
        calendarSyncModule.init()
        scheduleShareModule.init()
    }

    override fun onInitView(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        scheduleNavigationModule.init()

        NightModeViewUtils.checkNightModeChangedAnimation(this, viewBinding, viewModel)

        viewBinding.viewPagerSchedulePanel.registerOnPageChangeCallback(ScheduleViewPagerChangeCallback())

        viewBinding.layoutScheduleDateInfoBar.setOnSingleClickListener {
            viewModel.scrollToCurrentWeekNum()
        }

        scheduleInsetsModule.init()

        ScheduleControlPanel.addScrollToWeekListener(supportFragmentManager, this) {
            scrollToWeek(it)
        }

        ScheduleLaunchModule.tryShowEula(this)
        scheduleLaunchModule.checkUpgrade()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_schedule, menu)
        requireViewModel().apply {
            toolBarTintColor.value?.let {
                menu.setIconTint(it)
            }
            menu.findItem(R.id.menu_scheduleAlert)?.isVisible = scheduleAlert.value ?: false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_scheduleControlPanel -> requireViewModel().showScheduleControlPanel()
            R.id.menu_scheduleShare -> scheduleShareModule.shareSchedule(getCurrentShowWeekNum())
            R.id.menu_scheduleAlert -> requireViewModel().showScheduleAlertDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem) =
        scheduleNavigationModule.onNavigationItemSelected(item)

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
                scheduleNavigationModule.drawerToggle?.drawerArrowDrawable?.color = tintColor
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