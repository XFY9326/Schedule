package tool.xfy9326.schedule.utils.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.animation.doOnEnd
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.github.xfy9326.atools.io.utils.tryRecycle
import io.github.xfy9326.atools.ui.setListener
import io.github.xfy9326.atools.ui.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.beans.NightMode
import tool.xfy9326.schedule.beans.NightMode.Companion.modeInt
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.databinding.ActivityScheduleBinding
import tool.xfy9326.schedule.databinding.LayoutNavHeaderBinding
import tool.xfy9326.schedule.ui.vm.ScheduleViewModel
import tool.xfy9326.schedule.utils.drawToBitmap
import tool.xfy9326.schedule.utils.getDefaultBackgroundColor
import tool.xfy9326.schedule.utils.isUsingNightMode
import kotlin.math.max
import kotlin.math.sqrt

object NightModeViewUtils {
    private const val EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE = "ANIMATE_NIGHT_MODE_CHANGED_BUNDLE"
    private const val EXTRA_SET_NIGHT_MODE = "SET_NIGHT_MODE"
    private const val EXTRA_START_X = "START_X"
    private const val EXTRA_START_Y = "START_Y"
    private const val EXTRA_FINAL_RADIUS = "FINAL_RADIUS"

    fun checkNightModeChangedAnimation(activity: AppCompatActivity, viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel) {
        checkNightModeChangedAnimation(
            activity,
            viewBinding.navSchedule.getHeaderView(0),
            viewBinding.imageViewNightModeChangeMask,
            viewBinding.layoutScheduleRoot,
            viewBinding.drawerSchedule,
            isChangingNightMode = {
                viewModel.nightModeChanging.get()
            },
            onReadOldSurface = {
                viewModel.nightModeChangeOldSurface.read()
            },
            onNightModeChanged = {
                viewModel.nightModeChanging.set(false)
            }
        )
    }

    private fun checkNightModeChangedAnimation(
        activity: AppCompatActivity,
        navHeaderView: View,
        nightModeMask: ImageView,
        rootView: View,
        contentView: View,
        isChangingNightMode: () -> Boolean,
        onReadOldSurface: () -> Bitmap?,
        onNightModeChanged: () -> Unit,
    ) {
        val animationParams = activity.intent.getBundleExtra(EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE)
        val nightModeChangeButton = LayoutNavHeaderBinding.bind(navHeaderView).buttonNightModeChange
        if (animationParams != null && isChangingNightMode.invoke()) {
            activity.intent.removeExtra(EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE)
            animateNightModeChanged(
                activity,
                nightModeChangeButton,
                nightModeMask,
                rootView,
                contentView,
                animationParams.getFloat(EXTRA_START_X),
                animationParams.getFloat(EXTRA_START_Y),
                animationParams.getFloat(EXTRA_FINAL_RADIUS),
                animationParams.getBoolean(EXTRA_SET_NIGHT_MODE),
                onReadOldSurface,
                onNightModeChanged
            )
        } else {
            nightModeChangeButton.setImageResource(getNightModeIcon(activity.isUsingNightMode()))
        }
    }

    fun requestNightModeChange(activity: AppCompatActivity, viewBinding: ActivityScheduleBinding, viewModel: ScheduleViewModel, button: View) {
        requestNightModeChange(activity, viewBinding.layoutScheduleRoot, button) { oldSurface ->
            viewModel.nightModeChangeOldSurface.write(oldSurface)
        }
    }

    private fun requestNightModeChange(activity: AppCompatActivity, rootView: View, nightModeButton: View, onSaveSurface: (Bitmap) -> Unit) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val manuallyChangeNightMode = AppSettingsDataStore.nightModeTypeFlow.first() == NightMode.FOLLOW_SYSTEM

            val isUsingNightMode = activity.isUsingNightMode()
            prepareAnimateNightModeChanged(rootView, nightModeButton, !isUsingNightMode, activity.intent)
            val mask = try {
                activity.window.drawToBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                Bitmap.createBitmap(activity.window.decorView.measuredWidth, activity.window.decorView.measuredHeight, Bitmap.Config.ALPHA_8)
            }
            onSaveSurface(mask)

            val newMode = if (isUsingNightMode) {
                NightMode.DISABLED
            } else {
                NightMode.ENABLED
            }.also { mode ->
                AppSettingsDataStore.setNightModeType(mode)
            }.modeInt

            launch(Dispatchers.Main) {
                if (manuallyChangeNightMode) activity.showToast(R.string.manually_change_night_mode)
                activity.window.setWindowAnimations(R.style.AppTheme_NightModeTransitionAnimation)
                AppCompatDelegate.setDefaultNightMode(newMode)
            }
        }
    }

    private fun prepareAnimateNightModeChanged(rootView: View, animCenterView: View, setNightMode: Boolean, intent: Intent) {
        val startX = animCenterView.x + animCenterView.measuredWidth / 2f
        val startY = animCenterView.y + animCenterView.measuredHeight / 2f
        val viewWidth = rootView.measuredWidth
        val viewHeight = rootView.measuredHeight

        val finalRadius =
            max(
                sqrt((viewWidth - startX) * (viewWidth - startX) + (viewHeight - startY) * (viewHeight - startY)),
                sqrt(startX * startX + (viewHeight - startY) * (viewHeight - startY))
            )

        intent.putExtra(
            EXTRA_ANIMATE_NIGHT_MODE_CHANGED_BUNDLE, bundleOf(
                EXTRA_START_X to startX,
                EXTRA_START_Y to startY,
                EXTRA_FINAL_RADIUS to finalRadius,
                EXTRA_SET_NIGHT_MODE to setNightMode
            )
        )
    }

    private fun animateNightModeChanged(
        context: Context,
        button: AppCompatImageButton,
        nightModeMask: ImageView,
        rootView: View,
        contentView: View,
        startX: Float,
        startY: Float,
        finalRadius: Float,
        setNightMode: Boolean,
        onReadOldSurface: () -> Bitmap?,
        onNightModeChanged: () -> Unit,
    ) {
        onReadOldSurface()?.let { oldSurface ->
            nightModeMask.setImageBitmap(oldSurface)
            nightModeMask.isVisible = true
            contentView.isVisible = false
            // Original button image
            button.setImageResource(getNightModeIcon(!setNightMode))

            val animationDuration = context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()

            rootView.postOnAnimation {
                button.animate().scaleX(0f).scaleY(0f).setDuration(animationDuration / 2).setListener(doOnFinally = {
                    button.scaleX = 0f
                    button.scaleY = 0f
                    button.setImageResource(getNightModeIcon(setNightMode))
                    button.animate().scaleX(1f).scaleY(1f).setDuration(animationDuration / 2).setListener(doOnFinally = {
                        button.scaleX = 1f
                        button.scaleY = 1f
                    }).start()
                }).start()

                ViewAnimationUtils.createCircularReveal(contentView, startX.toInt(), startY.toInt(), 0f, finalRadius).apply {
                    duration = animationDuration
                    interpolator = AccelerateInterpolator()

                    doOnEnd {
                        nightModeMask.isVisible = false
                        contentView.background = null
                        nightModeMask.setImageDrawable(null)
                        oldSurface.tryRecycle()
                        onNightModeChanged()
                    }

                    contentView.background = context.getDefaultBackgroundColor().toDrawable()
                    contentView.isVisible = true
                }.start()
            }
        }
    }

    @DrawableRes
    private fun getNightModeIcon(nightMode: Boolean) = if (nightMode) R.drawable.ic_day_24 else R.drawable.ic_dark_mode_24
}