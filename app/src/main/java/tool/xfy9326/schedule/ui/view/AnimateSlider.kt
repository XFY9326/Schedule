package tool.xfy9326.schedule.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import com.google.android.material.slider.Slider
import tool.xfy9326.schedule.R

class AnimateSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = com.google.android.material.R.attr.sliderStyle,
) : Slider(context, attrs, defStyle), Slider.OnSliderTouchListener {
    private var initValue = -1f
    private var listener: ((Float) -> Unit)? = null

    private val animNotTouchedRadius = context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_not_touched)
    private val animTouchedRadius = context.resources.getDimensionPixelSize(R.dimen.slider_thumb_radius_touched)
    private val animTime = context.resources.getInteger(R.integer.very_short_anim_time).toLong()

    init {
        addOnSliderTouchListener(this)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> animateSlideThumb(true)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> animateSlideThumb(false)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onStartTrackingTouch(slider: Slider) {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onStopTrackingTouch(slider: Slider) {
        val currentValue = value
        if (initValue != currentValue) {
            initValue = currentValue
            listener?.invoke(currentValue)
        }
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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