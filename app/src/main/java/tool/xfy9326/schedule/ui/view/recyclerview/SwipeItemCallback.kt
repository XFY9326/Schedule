package tool.xfy9326.schedule.ui.view.recyclerview

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.animation.addListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.xfy9326.atools.ui.dpToPx
import io.github.xfy9326.atools.ui.tryStartAnimation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class SwipeItemCallback : ItemTouchHelper.Callback() {
    companion object {
        private val foregroundSwipeCorner = 10f.dpToPx()
        private const val foregroundCornerDuration = 150L
        private const val backgroundCircularRevealDuration = 400L
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
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
        if (viewHolder is SwipeItemViewHolder<*>) {
            makeMovementFlags(0, ItemTouchHelper.START)
        } else {
            makeMovementFlags(0, 0)
        }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is SwipeItemViewHolder<*>) listener?.invoke(viewHolder.adapterPosition)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float =
        if (viewHolder is SwipeItemViewHolder<*>) {
            swipeThreshold
        } else {
            super.getSwipeThreshold(viewHolder)
        }

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
                setupSwipeIcon(viewHolder, dX, isCurrentlyActive)
                setupBackground(viewHolder, dX)
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
        hasChangedCorner.set(false)
        hasAnimatedIcon.set(false)
        circularRevealAnimation?.cancel()
        viewHolder.backgroundSwipeView.visibility = View.INVISIBLE
        (viewHolder.foregroundSwipeView.background as? GradientDrawable)?.apply {
            mutate()
            cornerRadius = 0f
        }
    }

    private fun setupBackground(viewHolder: SwipeItemViewHolder<*>, dX: Float) {
        val notSwiped = viewHolder.backgroundSwipeView.measuredWidth * getSwipeThreshold(viewHolder) > abs(dX)

        if (notSwiped) {
            if (hasSetSwipeAction.compareAndSet(true, false)) {
                animateSwipeIcon(viewHolder)
                animateTransitionBackground(viewHolder, false)
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else {
            if (hasSetSwipeAction.compareAndSet(false, true)) {
                animateSwipeIcon(viewHolder)
                animateTransitionBackground(viewHolder, true)
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
        val edgeSwiped = if (viewHolder.itemView.layoutDirection == ViewGroup.LAYOUT_DIRECTION_LTR) {
            viewHolder.itemView.right + dX < viewHolder.swipeIconView.left
        } else {
            viewHolder.itemView.left + dX > viewHolder.swipeIconView.right
        }

        if (edgeSwiped) {
            if (hasAnimatedIcon.compareAndSet(true, false)) {
                viewHolder.swipeIconView.drawable.tryStartAnimation()
            }
        } else {
            if (hasAnimatedIcon.compareAndSet(false, true)) {
                if (isCurrentlyActive) viewHolder.swipeIconView.drawable.tryStartAnimation()
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
                    icon.cornerRadii =
                        if (viewHolder.foregroundSwipeView.layoutDirection == ViewGroup.LAYOUT_DIRECTION_LTR) {
                            floatArrayOf(0f, 0f, value, value, value, value, 0f, 0f)
                        } else {
                            floatArrayOf(value, value, 0f, 0f, 0f, 0f, value, value)
                        }
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

    private fun animateTransitionBackground(viewHolder: SwipeItemViewHolder<*>, showSwipeBackground: Boolean) {
        val viewWidth = viewHolder.backgroundSwipeView.measuredWidth
        val viewHeight = viewHolder.backgroundSwipeView.measuredHeight
        val iconX = viewHolder.swipeIconView.x + viewHolder.swipeIconView.measuredWidth / 2
        val iconY = viewHolder.swipeIconView.y + viewHolder.swipeIconView.measuredHeight / 2
        val maxRadius = max(
            sqrt((viewWidth - iconX) * (viewWidth - iconX) + (viewHeight - iconY) * (viewHeight - iconY)),
            sqrt(iconX * iconX + (viewHeight - iconY) * (viewHeight - iconY))
        )

        val startRadius = if (showSwipeBackground) 0f else maxRadius
        val finalRadius = if (showSwipeBackground) maxRadius else 0f

        circularRevealAnimation?.let {
            it.removeAllListeners()
            it.cancel()
        }

        circularRevealAnimation = ViewAnimationUtils.createCircularReveal(
            viewHolder.backgroundSwipeView, iconX.toInt(), iconY.toInt(), startRadius, finalRadius
        ).apply {
            duration = backgroundCircularRevealDuration
            addListener(
                onStart = {
                    if (showSwipeBackground) viewHolder.backgroundSwipeView.visibility = View.VISIBLE
                },
                onCancel = {
                    viewHolder.backgroundSwipeView.visibility = if (showSwipeBackground) View.VISIBLE else View.INVISIBLE
                },
                onEnd = {
                    if (!showSwipeBackground) viewHolder.backgroundSwipeView.visibility = View.INVISIBLE
                }
            )
            start()
        }
    }

    fun setOnItemSwipedListener(listener: ((Int) -> Unit)?) {
        this.listener = listener
    }
}