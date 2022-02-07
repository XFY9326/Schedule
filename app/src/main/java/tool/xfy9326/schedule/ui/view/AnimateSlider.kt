package tool.xfy9326.schedule.ui.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnCancel
import com.google.android.material.slider.Slider
import tool.xfy9326.schedule.R

class AnimateSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = com.google.android.material.R.attr.sliderStyle,
) : Slider(context, attrs, defStyle), Slider.OnSliderTouchListener {
    private var listener: ((Float) -> Unit)? = null

    private val animNotTouchedRadius = context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_not_touched)
    private val animTouchedRadius = context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_touched)
    private val animTime = context.resources.getInteger(R.integer.very_short_anim_time).toLong()

    init {
        addOnSliderTouchListener(this)
    }

    @SuppressLint("RestrictedApi")
    override fun onStartTrackingTouch(slider: Slider) {
        animateSlideThumb(true)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    @SuppressLint("RestrictedApi")
    override fun onStopTrackingTouch(slider: Slider) {
        animateSlideThumb(false)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        listener?.invoke(value)
    }

    private fun animateSlideThumb(isTouched: Boolean) {
        clearAnimation()
        val currentRadius = if (isTouched) animNotTouchedRadius else animTouchedRadius
        val targetRadius = if (isTouched) animTouchedRadius else animNotTouchedRadius
        ValueAnimator.ofInt(currentRadius, targetRadius).apply {
            duration = animTime
            interpolator = DecelerateInterpolator()
            addUpdateListener { thumbRadius = it.animatedValue as Int }
            doOnCancel {
                thumbRadius = targetRadius
            }
            start()
        }
    }

    fun setOnSlideValueSetListener(listener: ((Float) -> Unit)?) {
        this.listener = listener
    }
}