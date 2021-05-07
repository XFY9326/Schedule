package tool.xfy9326.schedule.ui.activity

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.xfy9326.livedata.observeEvent
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.ImageScareType
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.data.AppDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.databinding.LayoutNavHeaderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.ScheduleViewPagerAdapter
import tool.xfy9326.schedule.ui.dialog.CourseDetailDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleControlPanel
import tool.xfy9326.schedule.ui.dialog.UpgradeDialog
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.*
import tool.xfy9326.schedule.utils.ics.ScheduleICSHelper
import tool.xfy9326.schedule.utils.view.DialogUtils
import tool.xfy9326.schedule.utils.view.NightModeViewUtils

class ScheduleActivity : ViewModelActivity<ScheduleViewModel, ActivityScheduleBinding>(), NavigationView.OnNavigationItemSelectedListener {
    override val vmClass = ScheduleViewModel::class

    private var scheduleViewPagerAdapter: ScheduleViewPagerAdapter? = null

    private val requestCalendarPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (PermissionUtils.checkGrantResults(it)) {
            requireViewModel().syncToCalendar()
        } else {
            requireViewBinding().layoutSchedule.showSnackBar(R.string.calendar_permission_get_failed)
        }
    }
    private val exportICSFile = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            requireViewModel().exportICS(it)
        } else {
            requireViewModel().waitExportScheduleId.consume()
            requireViewBinding().layoutSchedule.showSnackBar(R.string.output_file_cancel)
        }
    }

    override fun onCreateViewBinding() = ActivityScheduleBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setSupportActionBar(viewBinding.toolBarSchedule)
        viewBinding.viewPagerSchedulePanel.offscreenPageLimit = 1
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        viewModel.weekNumInfo.observe(this) {
            setupViewPager(it.first, it.second)
            refreshToolBarTime(it.first)
        }
        viewModel.nowDay.observe(this, ::updateDate)
        viewModel.scheduleBackground.observe(this, ::onChangeScheduleBackground)
        viewModel.showWeekChanged.observeEvent(this) {
            updateShowWeekNum(it.first, it.second)
        }
        viewModel.scrollToWeek.observeEvent(this, observer = ::scrollToWeek)
        viewModel.showScheduleControlPanel.observeEvent(this) {
            ScheduleControlPanel.showDialog(supportFragmentManager, getCurrentShowWeekNum(), it.first, it.second)
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
        viewModel.selectScheduleForExportingICS.observeEvent(this) {
            DialogUtils.showScheduleSelectDialog(this, R.string.export_to_ics, it) { name, id ->
                viewModel.waitExportScheduleId.write(id)
                exportICSFile.launch(ScheduleICSHelper.createICSFileName(this, name))
            }
        }
        viewModel.iceExportStatus.observeEvent(this) {
            viewBinding.layoutSchedule.showSnackBar(if (it) R.string.output_file_success else R.string.output_file_failed)
        }
        viewModel.syncToCalendarStatus.observeEvent(this) {
            if (it.success) {
                if (it.failedAmount == 0) {
                    viewBinding.layoutSchedule.showSnackBar(R.string.calendar_sync_success)
                } else {
                    viewBinding.layoutSchedule.showSnackBar(R.string.calendar_sync_failed, it.total, it.failedAmount)
                }
            } else {
                viewBinding.layoutSchedule.showSnackBar(R.string.calendar_sync_error)
            }
        }
        viewModel.toolBarTintColor.observe(this, ::setToolBarTintColor)
        viewModel.useLightColorSystemBarColor.observe(this) {
            // Light status bar in Android Window means status bar that used in light background, so the status bar color is black.
            // For default, it's true in app theme.
            window.enableLightSystemBar(this, !it && !isUsingNightMode())
        }
        viewModel.scheduleShared.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutSchedule.showSnackBar(R.string.generate_share_schedule_failed)
            } else {
                startActivity(IntentUtils.getShareImageIntent(this, it))
            }
        }
        viewModel.onlineCourseImportEnabled.observe(this) {
            viewBinding.navSchedule.menu.findItem(R.id.menu_navOnlineCourseImport)?.isVisible = it
        }
    }

    override fun onInitView(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setupDrawer(viewBinding, viewModel)
        NightModeViewUtils.checkNightModeChangedAnimation(this, viewBinding, viewModel)

        viewBinding.viewPagerSchedulePanel.registerOnPageChangeCallback(ScheduleViewPagerChangeCallback())

        viewBinding.layoutScheduleDateInfoBar.setOnClickListener {
            viewModel.scrollToCurrentWeekNum()
        }

        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            WindowInsetsCompat.toWindowInsetsCompat(insets).apply {
                val layoutParams = (viewBinding.layoutScheduleContent.layoutParams as? ViewGroup.MarginLayoutParams)
                if (layoutParams == null) {
                    viewBinding.layoutScheduleContent.updatePadding(top = systemWindowInsetTop)
                } else {
                    layoutParams.updateMargins(top = systemWindowInsetTop)
                }
            }.consumeSystemWindowInsets().toWindowInsets()
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
            R.id.menu_scheduleShare -> {
                requireViewBinding().layoutSchedule.showSnackBar(R.string.generating_share_schedule)
                requireViewModel().shareScheduleImage(getCurrentShowWeekNum(), resources.displayMetrics.widthPixels)
            }
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
                requireViewModel().selectScheduleForExportingICS()
                delayCloseDrawer = false
            }
            R.id.menu_navSyncToCalendar -> {
                withShownCalendarSyncAttention {
                    syncScheduleToCalendar()
                }
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

    private fun withShownCalendarSyncAttention(block: () -> Unit) {
        lifecycleScope.launch {
            if (AppDataStore.hasShownCalendarSyncAttention()) {
                block()
            } else {
                DialogUtils.showCalendarSyncAttentionDialog(this@ScheduleActivity) {
                    block()
                }
            }
        }
    }

    private fun syncScheduleToCalendar() {
        lifecycleScope.launch {
            if (PermissionUtils.checkCalendarPermission(this@ScheduleActivity, requestCalendarPermission)) {
                requireViewModel().syncToCalendar()
            }
        }
    }

    private fun onChangeScheduleBackground(bundle: ScheduleDataStore.ScheduleBackgroundBuildBundle?) {
        requireViewBinding().imageViewScheduleBackground.apply {
            if (bundle == null) {
                setImageDrawable(null)
            } else {
                scaleType = when (bundle.scareType) {
                    ImageScareType.FIT_CENTER -> ImageView.ScaleType.FIT_CENTER
                    ImageScareType.CENTER_CROP -> ImageView.ScaleType.CENTER_CROP
                    ImageScareType.CENTER_INSIDE -> ImageView.ScaleType.CENTER_INSIDE
                }
                load(bundle.file) {
                    if (bundle.useAnim) crossfade(true)
                }
            }
        }
    }

    private fun setToolBarTintColor(color: Int?) {
        val tintColor = color ?: getColorCompat(R.color.schedule_tool_bar_tint)
        requireViewBinding().apply {
            textViewScheduleTodayDate.setTextColor(tintColor)
            textViewScheduleNotCurrentWeek.setTextColor(tintColor)
            textViewScheduleNowShowWeekNum.setTextColor(tintColor)
            toolBarSchedule.navigationIcon?.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC)
            toolBarSchedule.menu.setIconTint(tintColor)
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
            LayoutNavHeaderBinding.bind(getHeaderView(0)).buttonNightModeChange.setOnClickListener {
                if (requireViewModel().nightModeChanging.compareAndSet(false, true)) {
                    it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    NightModeViewUtils.requestNightModeChange(this@ScheduleActivity, viewBinding, viewModel, it)
                }
            }
        }

        ActionBarDrawerToggle(this, viewBinding.drawerSchedule, viewBinding.toolBarSchedule, R.string.open_drawer, R.string.close_drawer).apply {
            drawerArrowDrawable.color = getColorCompat(R.color.dark_icon)
            isDrawerIndicatorEnabled = true
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