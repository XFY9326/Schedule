package tool.xfy9326.schedule.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.*
import android.view.animation.AccelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.animation.doOnEnd
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.Day
import tool.xfy9326.schedule.beans.ImageScareType
import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.WeekNumType
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.data.ScheduleDataStore
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.databinding.LayoutNavHeaderBinding
import tool.xfy9326.schedule.kt.*
import tool.xfy9326.schedule.tools.MIMEConst
import tool.xfy9326.schedule.ui.activity.base.ViewModelActivity
import tool.xfy9326.schedule.ui.adapter.ScheduleViewPagerAdapter
import tool.xfy9326.schedule.ui.dialog.CourseDetailDialog
import tool.xfy9326.schedule.ui.dialog.ScheduleControlPanel
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.DialogUtils
import tool.xfy9326.schedule.utils.IntentUtils
import tool.xfy9326.schedule.utils.PermissionUtils
import tool.xfy9326.schedule.utils.ScheduleICSHelper
import kotlin.math.max
import kotlin.math.sqrt

class ScheduleActivity : ViewModelActivity<ScheduleViewModel, ActivityScheduleBinding>(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE = "ANIMATE_NIGHT_MODE_CHANGED_BUNDLE"
        private const val EXTRA_SET_NIGHT_MODE = "SET_NIGHT_MODE"
        private const val EXTRA_START_X = "START_X"
        private const val EXTRA_START_Y = "START_Y"
        private const val EXTRA_FINAL_RADIUS = "FINAL_RADIUS"

        private const val REQUEST_CODE_EXPORT_ICS_FILE = 1
        private const val REQUEST_CODE_CALENDAR_PERMISSION = 2
    }

    private var scheduleViewPagerAdapter: ScheduleViewPagerAdapter? = null

    override fun onCreateViewModel(owner: ViewModelStoreOwner): ScheduleViewModel = ViewModelProvider(owner)[ScheduleViewModel::class.java]

    override fun onCreateViewBinding() = ActivityScheduleBinding.inflate(layoutInflater)

    override fun onPrepare(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setSupportActionBar(viewBinding.toolBarSchedule)
        viewBinding.viewPagerSchedulePanel.offscreenPageLimit = 1

        checkNightModeChangedAnimation()
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
        viewModel.selectScheduleForExportingICS.observeEvent(this) {
            DialogUtils.showScheduleSelectDialog(this, R.string.export_to_ics, it) { name, id ->
                viewModel.waitExportScheduleId.write(id)
                tryStartActivityForResult(
                    IntentUtils.getCreateNewDocumentIntent(ScheduleICSHelper.createICSFileName(this, name), MIMEConst.MIME_TEXT_CALENDAR),
                    REQUEST_CODE_EXPORT_ICS_FILE
                )
            }
        }
        viewModel.iceExportStatus.observeEvent(this) {
            viewBinding.layoutSchedule.showShortSnackBar(if (it) R.string.output_file_success else R.string.output_file_failed)
        }
        viewModel.syncToCalendarStatus.observeEvent(this) {
            if (it.success) {
                if (it.failedAmount == 0) {
                    viewBinding.layoutSchedule.showShortSnackBar(R.string.calendar_sync_success)
                } else {
                    viewBinding.layoutSchedule.showShortSnackBar(R.string.calendar_sync_failed, it.total, it.failedAmount)
                }
            } else {
                viewBinding.layoutSchedule.showShortSnackBar(R.string.calendar_sync_error)
            }
        }
        viewModel.scheduleBackground.observe(this, ::onChangeScheduleBackground)
        viewModel.toolBarTintColor.observe(this, ::setToolBarTintColor)
        viewModel.useLightColorSystemBarColor.observe(this) {
            // Light status bar in Android Window means status bar that used in light background, so the status bar color is black.
            // For default, it's true in app theme.
            window.enableLightSystemBar(this, !it && !isUsingNightMode())
        }
        viewModel.scheduleShared.observeEvent(this) {
            if (it == null) {
                viewBinding.layoutSchedule.showShortSnackBar(R.string.generate_share_schedule_failed)
            } else {
                startActivity(IntentUtils.getShareImageIntent(this, it))
            }
        }
    }

    override fun onInitView(viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        setupDrawer(viewBinding)

        viewBinding.viewPagerSchedulePanel.registerOnPageChangeCallback(ScheduleViewPagerChangeCallback())

        viewBinding.layoutScheduleDateInfoBar.setOnClickListener {
            viewModel.scrollToCurrentWeekNum()
        }

        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            WindowInsetsCompat.toWindowInsetsCompat(insets).apply {
                viewBinding.layoutScheduleContent.updatePadding(top = systemWindowInsetTop)
            }.consumeSystemWindowInsets().toWindowInsets()
        }
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
                requireViewBinding().layoutSchedule.showShortSnackBar(R.string.generating_share_schedule)
                requireViewModel().shareScheduleImage(getCurrentShowWeekNum(), resources.displayMetrics.widthPixels)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_EXPORT_ICS_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val outputUri = data?.data
                if (outputUri != null) {
                    requireViewModel().exportICS(outputUri)
                } else {
                    requireViewBinding().layoutSchedule.showShortSnackBar(R.string.output_file_create_failed)
                }
            } else {
                requireViewModel().waitExportScheduleId.consume()
                requireViewBinding().layoutSchedule.showShortSnackBar(R.string.output_file_cancel)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (requireViewBinding().drawerSchedule.isDrawerOpen(GravityCompat.START)) {
            requireViewBinding().drawerSchedule.closeDrawers()
        } else {
            requireViewModel().exitAppDirectly()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_CALENDAR_PERMISSION) {
            if (PermissionUtils.checkGrantResults(grantResults)) {
                requireViewModel().syncToCalendar()
            } else {
                requireViewBinding().layoutSchedule.showShortSnackBar(R.string.calendar_permission_get_failed)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_navCourseImport -> startActivity<OnlineCourseImportActivity>()
            R.id.menu_navCourseExportICS -> requireViewModel().selectScheduleForExportingICS()
            R.id.menu_navSyncToCalendar -> lifecycleScope.launch {
                if (PermissionUtils.checkCalendarPermission(this@ScheduleActivity, REQUEST_CODE_CALENDAR_PERMISSION)) {
                    requireViewModel().syncToCalendar()
                }
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
            delay(resources.getInteger(R.integer.drawer_close_anim_time).toLong())
            requireViewBinding().drawerSchedule.closeDrawers()
        }
        return true
    }

    private fun onChangeScheduleBackground(bundle: ScheduleDataStore.ScheduleBackgroundBuildBundle?) {
        requireViewBinding().imageViewScheduleBackground.apply {
            if (bundle == null) {
                setImageDrawable(null)
            } else {
                alpha = bundle.alpha
                Glide.with(this).load(bundle.file).apply {
                    when (bundle.scareType) {
                        ImageScareType.FIT_CENTER -> fitCenter()
                        ImageScareType.CENTER_CROP -> centerCrop()
                        ImageScareType.CENTER_INSIDE -> centerInside()
                    }
                }.apply {
                    if (bundle.fadeAnim) transition(DrawableTransitionOptions.withCrossFade())
                }.into(this)
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
            LayoutNavHeaderBinding.bind(getHeaderView(0)).buttonNightModeChange.setOnClickListener {
                if (requireViewModel().nightModeChanging.compareAndSet(false, true)) {
                    it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    lifecycleScope.launch(Dispatchers.Default) {
                        val manuallyChangeNightMode = AppSettingsDataStore.nightModeTypeFlow.first() == NightMode.FOLLOW_SYSTEM
                        val newMode = if (isUsingNightMode()) {
                            prepareAnimateNightModeChanged(it, false)
                            NightMode.DISABLED
                        } else {
                            prepareAnimateNightModeChanged(it, true)
                            NightMode.ENABLED
                        }.also { mode ->
                            AppSettingsDataStore.setNightModeType(mode)
                        }.modeInt
                        launch(Dispatchers.Main.immediate) {
                            if (manuallyChangeNightMode) showShortToast(R.string.manually_change_night_mode)
                            window.setWindowAnimations(R.style.AppTheme_NightModeTransitionAnimation)
                            AppCompatDelegate.setDefaultNightMode(newMode)
                        }
                    }
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

    private fun prepareAnimateNightModeChanged(animCenterView: View, setNightMode: Boolean) {
        requireViewBinding().apply {
            val startX = animCenterView.x + animCenterView.measuredWidth / 2f
            val startY = animCenterView.y + animCenterView.measuredHeight / 2f
            val viewWidth = layoutScheduleRoot.measuredWidth
            val viewHeight = layoutScheduleRoot.measuredHeight

            requireViewModel().nightModeChangeOldSurface.write(window.decorView.drawToBitmap())

            val finalRadius =
                max(sqrt((viewWidth - startX) * (viewWidth - startX) + (viewHeight - startY) * (viewHeight - startY)),
                    sqrt(startX * startX + (viewHeight - startY) * (viewHeight - startY)))

            intent.putExtra(EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE, buildBundle {
                putFloat(EXTRA_START_X, startX)
                putFloat(EXTRA_START_Y, startY)
                putFloat(EXTRA_FINAL_RADIUS, finalRadius)
                putBoolean(EXTRA_SET_NIGHT_MODE, setNightMode)
            })
        }
    }

    private fun checkNightModeChangedAnimation() {
        val animationParams = intent?.getBundleExtra(EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE)
        val nightModeChangeButton = LayoutNavHeaderBinding.bind(requireViewBinding().navSchedule.getHeaderView(0)).buttonNightModeChange
        if (animationParams != null) {
            intent.removeExtra(EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE)
            animateNightModeChanged(
                nightModeChangeButton,
                animationParams.getFloat(EXTRA_START_X),
                animationParams.getFloat(EXTRA_START_Y),
                animationParams.getFloat(EXTRA_FINAL_RADIUS),
                animationParams.getBoolean(EXTRA_SET_NIGHT_MODE)
            )
        } else {
            nightModeChangeButton.setImageResource(getNightModeIcon(isUsingNightMode()))
        }
    }

    private fun animateNightModeChanged(button: AppCompatImageButton, startX: Float, startY: Float, finalRadius: Float, setNightMode: Boolean) {
        requireViewModel().nightModeChangeOldSurface.read()?.let { oldSurface ->

            requireViewBinding().apply {
                imageViewNightModeChangeMask.setImageBitmap(oldSurface)
                imageViewNightModeChangeMask.isVisible = true
                drawerSchedule.isVisible = false
                // Original button image
                button.setImageResource(getNightModeIcon(!setNightMode))

                val animationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()

                layoutScheduleRoot.postOnAnimation {
                    button.animate().scaleX(0f).scaleY(0f).setDuration(animationDuration / 2).withEndAction {
                        button.setImageResource(getNightModeIcon(setNightMode))
                        button.animate().scaleX(1f).scaleY(1f).setDuration(animationDuration / 2).start()
                    }.start()

                    ViewAnimationUtils.createCircularReveal(drawerSchedule, startX.toInt(), startY.toInt(), 0f, finalRadius).apply {
                        duration = animationDuration
                        interpolator = AccelerateInterpolator()

                        doOnEnd {
                            imageViewNightModeChangeMask.isVisible = false
                            drawerSchedule.background = null
                            imageViewNightModeChangeMask.setImageDrawable(null)
                            oldSurface.recycle()
                            requireViewModel().nightModeChanging.set(false)
                        }

                        drawerSchedule.background = getDefaultBackgroundColor().toDrawable()
                        drawerSchedule.isVisible = true
                    }.start()
                }
            }
        }
    }

    @DrawableRes
    private fun getNightModeIcon(nightMode: Boolean) =
        if (nightMode) {
            R.drawable.ic_day_24
        } else {
            R.drawable.ic_dark_mode_24
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