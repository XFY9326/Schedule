package tool.xfy9326.schedule.ui.activity

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.navigation.NavigationView
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.ImageScareType
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.kt.enableLightStatusBar
import tool.xfy9326.schedule.kt.getColorCompat
import tool.xfy9326.schedule.kt.observeEvent
import tool.xfy9326.schedule.kt.startActivity
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.ScheduleViewPagerAdapter
import tool.xfy9326.schedule.ui.dialog.CourseDetailDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleControlPanel
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel

class ScheduleActivity : ViewModelActivity<ScheduleViewModel, ActivityScheduleBinding>(), NavigationView.OnNavigationItemSelectedListener {
    private var scheduleViewPagerAdapter: ScheduleViewPagerAdapter? = null

    override fun onCreateViewModel(owner: ViewModelStoreOwner): ScheduleViewModel = ViewModelProvider(owner)[ScheduleViewModel::class.java]

    override fun onCreateViewBinding() = ActivityScheduleBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setSupportActionBar(viewBinding.toolBarSchedule)
        viewBinding.viewPagerSchedulePanel.offscreenPageLimit = 1
    }

    override fun onBindLiveData(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        viewModel.weekNumInfo.observe(this) {
            refreshToolBarTime(it.first)
            setupViewPager(it.first, it.second)
        }
        viewModel.nowDay.observe(this, ::updateDate)
        viewModel.showWeekChanged.observeEvent(this) {
            updateShowWeekNum(it.first, it.second)
        }
        viewModel.scrollToWeek.observeEvent(this, observer = ::scrollToWeek)
        viewModel.showScheduleControlPanel.observeEvent(this) {
            ScheduleControlPanel.showDialog(supportFragmentManager, getCurrentShowWeekNum(), it.first, it.second)
        }
        viewModel.showCourseDetailDialog.observeEvent(this) {
            CourseDetailDialog.showDialog(supportFragmentManager, it.second, it.first, it.third)
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
        viewModel.scheduleBackground.observe(this, ::onChangeScheduleBackground)
        viewModel.toolBarTintColor.observe(this, ::setToolBarTintColor)
        viewModel.useLightColorStatusBarColor.observe(this) {
            window.enableLightStatusBar(!it)
        }
    }

    override fun onInitView(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setupDrawer(viewBinding)

        viewBinding.viewPagerSchedulePanel.registerOnPageChangeCallback(ScheduleViewPagerChangeCallback())

        viewBinding.layoutScheduleDateInfoBar.setOnClickListener {
            viewModel.scrollToCurrentWeekNum()
        }

        window.decorView.setOnApplyWindowInsetsListener { v, insets ->
            WindowInsetsCompat.toWindowInsetsCompat(insets).apply {
                v.updatePadding(bottom = -systemWindowInsetBottom)
            }.toWindowInsets()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_schedule, menu)
        val tintColor = requireViewModel().toolBarTintColor.value
        if (tintColor != null) {
            menu?.iterator()?.forEach {
                it.icon?.setTint(tintColor)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_scheduleControlPanel) {
            requireViewModel().showScheduleControlPanel()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        requireViewModel().exitAppDirectly()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_navCourseImport -> startActivity<CourseImportActivity>()
            R.id.menu_navScheduleManage -> startActivity<ScheduleManageActivity>()
            R.id.menu_navCourseManage -> requireViewModel().openCurrentScheduleCourseManageActivity()
            R.id.menu_navSettings -> startActivity<SettingsActivity>()
            R.id.menu_navExit -> {
                finishAndRemoveTask()
                return true
            }
        }
        requireViewBinding().drawerSchedule.let {
            it.postDelayed(resources.getInteger(android.R.integer.config_shortAnimTime).toLong()) {
                it.closeDrawers()
            }
        }
        return true
    }

    private fun onChangeScheduleBackground(bundle: ScheduleDataStore.ScheduleBackgroundBuildBundle?) {
        requireViewBinding().imageViewScheduleBackground.apply {
            if (bundle == null) {
                isVisible = false
                setImageDrawable(null)
            } else {
                isVisible = true
                alpha = bundle.alpha
                Glide.with(this).load(bundle.bitmap).apply {
                    when (bundle.scareType) {
                        ImageScareType.FIT_CENTER -> fitCenter()
                        ImageScareType.CENTER_CROP -> centerCrop()
                        ImageScareType.CENTER_INSIDE -> centerInside()
                    }
                }.transition(DrawableTransitionOptions.withCrossFade()).into(this)
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
            toolBarSchedule.menu.iterator().forEach {
                it.icon?.setTint(tintColor)
            }
        }
    }

    private fun getCurrentShowWeekNum() = requireViewBinding().viewPagerSchedulePanel.currentItem + 1

    private fun refreshToolBarTime(nowWeekNum: Int) {
        val useWeekNum = if (requireViewModel().currentScrollPosition == null) {
            nowWeekNum
        } else {
            getCurrentShowWeekNum()
        }
        updateShowWeekNum(useWeekNum, WeekNumType.create(useWeekNum, nowWeekNum))
    }

    private fun setupViewPager(nowWeekNum: Int, maxWeekNum: Int) {
        var position = nowWeekNum
        if (nowWeekNum != 0) position--

        requireViewBinding().viewPagerSchedulePanel.apply {
            if (adapter == null) {
                scheduleViewPagerAdapter = ScheduleViewPagerAdapter(this@ScheduleActivity, maxWeekNum)
                adapter = scheduleViewPagerAdapter
                setCurrentItem(requireViewModel().currentScrollPosition ?: position, false)
            } else {
                scheduleViewPagerAdapter?.updateMaxWeekNum(maxWeekNum)
            }
        }
    }

    private fun setupDrawer(viewBinding: ActivityScheduleBinding) {
        viewBinding.navSchedule.apply {
            setNavigationItemSelectedListener(this@ScheduleActivity)
            getChildAt(0)?.isVerticalScrollBarEnabled = false
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