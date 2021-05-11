package tool.xfy9326.schedule.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.animation.LinearInterpolator
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

    override fun onStartTrackingTouch(slider: Slider) {
        animateSlideThumb(true)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onStopTrackingTouch(slider: Slider) {
        animateSlideThumb(false)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        listener?.invoke(value)
    }

    private fun animateSlideThumb(isTouched: Boolean) {
        clearAnimation()
        if (isTouched) {
            ValueAnimator.ofInt(animNotTouchedRadius, animTouchedRadius)
        } else {
            ValueAnimator.ofInt(animTouchedRadius, animNotTouchedRadius)
        }.apply {
            duration = animTime
            interpolator = LinearInterpolator()
            addUpdateListener { thumbRadius = it.animatedValue as Int }
            start()
        }
    }

    fun setOnSlideValueSetListener(listener: ((Float) -> Unit)?) {
        this.listener = listener
    }
}