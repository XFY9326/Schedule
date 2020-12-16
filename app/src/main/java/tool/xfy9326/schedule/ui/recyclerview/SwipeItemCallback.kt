package tool.xfy9326.schedule.ui.recyclerview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.schedule.kt.dpToPx
import tool.xfy9326.schedule.kt.isAnimatedVectorDrawable
import tool.xfy9326.schedule.kt.startAnimateDrawable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

class SwipeItemCallback : ItemTouchHelper.Callback() {
    companion object {
        private val foregroundSwipeCorner = 10f.dpToPx()
        private const val foregroundCornerDuration = 150L
        private const val backgroundCircularRevealDuration = 380L
        private const val swipeIconDuration = 100L
        private const val swipeIconScaleTo = 1.2f
        private const val swipeThreshold = 0.3f
        private const val swipeVelocityThreshold = 0.8f
    }

    private var listener: ((Int) -> Unit)? = null

    private val hasSetSwipeAction = AtomicBoolean(false)
    private val hasChangedCorner = AtomicBoolean(false)
    private val hasAnimatedIcon = AtomicBoolean(false)

    private var circularRevealAnimation: Animator? = null

    override fun isItemViewSwipeEnabled(): Boolean = true
    override fun isLongPressDragEnabled(): Boolean = false
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int = makeMovementFlags(0, ItemTouchHelper.START)

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is SwipeItemViewHolder<*>) listener?.invoke(viewHolder.adapterPosition)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = swipeThreshold

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float = swipeVelocityThreshold

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        if (viewHolder is SwipeItemViewHolder<*>) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                setupForegroundCorner(viewHolder, dX)
                if (viewHolder.swipeIconView.drawable.isAnimatedVectorDrawable()) {
                    setupSwipeIcon(viewHolder, dX, isCurrentlyActive)
                }
                setupBackground(recyclerView, viewHolder, dX)
            } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                if (dX <= 0) resetSwipeView(viewHolder)
            }
            getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.foregroundSwipeView, dX, dY, actionState, isCurrentlyActive)
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is SwipeItemViewHolder<*>) {
            resetSwipeView(viewHolder)
            getDefaultUIUtil().clearView(viewHolder.foregroundSwipeView)
        } else {
            super.clearView(recyclerView, viewHolder)
        }
    }

    private fun resetSwipeView(viewHolder: SwipeItemViewHolder<*>) {
        hasSetSwipeAction.set(false)
        circularRevealAnimation?.cancel()
        viewHolder.backgroundSwipeView.visibility = View.INVISIBLE
        (viewHolder.foregroundSwipeView.background as? GradientDrawable)?.apply {
            mutate()
            cornerRadius = 0f
        }
    }

    private fun setupBackground(recyclerView: RecyclerView, viewHolder: SwipeItemViewHolder<*>, dX: Float) {
        val notSwiped = recyclerView.width * getSwipeThreshold(viewHolder) > abs(dX)

        if (notSwiped) {
            if (hasSetSwipeAction.compareAndSet(true, false)) {
                animateSwipeIcon(viewHolder)
                animateTransitionBackground(viewHolder, dX, false)
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else {
            if (hasSetSwipeAction.compareAndSet(false, true)) {
                animateSwipeIcon(viewHolder)
                animateTransitionBackground(viewHolder, dX, true)
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    private fun setupForegroundCorner(viewHolder: SwipeItemViewHolder<*>, dX: Float) {
        if (abs(dX) < 1f) {
            if (hasChangedCorner.compareAndSet(true, false)) {
                animateForegroundCorner(viewHolder, foregroundSwipeCorner, 0f)
            }
        } else {
            if (hasChangedCorner.compareAndSet(false, true)) {
                animateForegroundCorner(viewHolder, 0f, foregroundSwipeCorner)
            }
        }
    }

    private fun setupSwipeIcon(viewHolder: SwipeItemViewHolder<*>, dX: Float, isCurrentlyActive: Boolean) {
        if (viewHolder.itemView.right + dX < viewHolder.swipeIconView.left) {
            if (hasAnimatedIcon.compareAndSet(true, false)) {
                viewHolder.swipeIconView.drawable.startAnimateDrawable()
            }
        } else {
            if (hasAnimatedIcon.compareAndSet(false, true)) {
                if (isCurrentlyActive) viewHolder.swipeIconView.drawable.startAnimateDrawable()
            }
        }
    }

    private fun animateForegroundCorner(viewHolder: SwipeItemViewHolder<*>, from: Float, to: Float) {
        (viewHolder.foregroundSwipeView.background as? GradientDrawable)?.let { icon ->
            ValueAnimator.ofFloat(from, to).apply {
                duration = foregroundCornerDuration
                addUpdateListener {
                    val value = it.animatedValue as Float
                    icon.mutate()
                    icon.cornerRadii = floatArrayOf(0f, 0f, value, value, value, value, 0f, 0f)
                }
                start()
            }
        }
    }

    private fun animateSwipeIcon(viewHolder: SwipeItemViewHolder<*>) {
        ScaleAnimation(1f, swipeIconScaleTo, 1f, swipeIconScaleTo, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = swipeIconDuration
            repeatCount = 1
            repeatMode = Animation.REVERSE
            viewHolder.swipeIconView.startAnimation(this)
        }
    }

    private fun animateTransitionBackground(viewHolder: SwipeItemViewHolder<*>, dX: Float, showSwipeBackground: Boolean) {
        val iconX = viewHolder.swipeIconView.x + viewHolder.swipeIconView.width / 2
        val iconY = viewHolder.swipeIconView.y + viewHolder.swipeIconView.height / 2
        val startRadius = if (showSwipeBackground) {
            0f
        } else {
            viewHolder.foregroundSwipeView.right + dX
        }
        val finalRadius = if (showSwipeBackground) {
            viewHolder.swipeIconView.x
        } else {
            0f
        }

        circularRevealAnimation?.let {
            it.removeAllListeners()
            it.cancel()
        }

        circularRevealAnimation = ViewAnimationUtils.createCircularReveal(
            viewHolder.backgroundSwipeView, iconX.toInt(), iconY.toInt(), startRadius, finalRadius
        ).apply {
            duration = backgroundCircularRevealDuration
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    if (showSwipeBackground) viewHolder.backgroundSwipeView.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    viewHolder.backgroundSwipeView.visibility = if (showSwipeBackground) View.VISIBLE else View.INVISIBLE
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!showSwipeBackground) viewHolder.backgroundSwipeView.visibility = View.INVISIBLE
                }
            })
            start()
        }
    }

    fun setOnItemSwipedListener(listener: ((Int) -> Unit)?) {
        this.listener = listener
    }
}