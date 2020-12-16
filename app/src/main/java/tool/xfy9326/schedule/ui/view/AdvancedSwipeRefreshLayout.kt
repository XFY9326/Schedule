@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package tool.xfy9326.schedule.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.content.res.use
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import tool.xfy9326.schedule.R
import kotlin.math.abs

class AdvancedSwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SwipeRefreshLayout(context, attrs) {
    private var mTouchSlop = 0
    private var startY = 0f
    private var startX = 0f
    private var mIsVpDrag = false

    var fitViewPager = false

    init {
        setAttrs(context, attrs)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    fun postStopRefreshing() = post {
        isRefreshing = false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (fitViewPager) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = ev.y
                    startX = ev.x
                    mIsVpDrag = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mIsVpDrag) {
                        return false
                    }
                    val endY = ev.y
                    val endX = ev.x
                    val distanceX = abs(endX - startX)
                    val distanceY = abs(endY - startY)
                    if (distanceX > mTouchSlop && distanceX > distanceY) {
                        mIsVpDrag = true
                        return false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mIsVpDrag = false
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun setAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.AdvancedSwipeRefreshLayout).use {
                val colorSchemeResId = it.getResourceId(R.styleable.AdvancedSwipeRefreshLayout_color_scheme, 0)
                val triggerAsyncDistanceResId = it.getResourceId(R.styleable.AdvancedSwipeRefreshLayout_trigger_async_distance, 0)
                if (colorSchemeResId != 0) {
                    setColorSchemeColors(*context.resources.getIntArray(colorSchemeResId))
                }
                if (triggerAsyncDistanceResId != 0) {
                    setDistanceToTriggerSync(context.resources.getInteger(triggerAsyncDistanceResId))
                }
            }
        }
    }
}