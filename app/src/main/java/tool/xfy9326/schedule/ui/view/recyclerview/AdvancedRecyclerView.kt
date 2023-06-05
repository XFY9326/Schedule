@file:Suppress("MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.ui.view.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.schedule.R
import kotlin.math.abs

class AdvancedRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.recyclerview.R.attr.recyclerViewStyle,
) : RecyclerView(context, attrs, defStyleAttr) {
    init {
        setAttrs(context, attrs)
    }

    var fitViewPager = false

    private var startX = 0f
    private var startY = 0f
    private var emptyViewResId = 0

    private fun setAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.AdvancedRecyclerView).use {
                emptyViewResId = it.getResourceId(R.styleable.AdvancedRecyclerView_empty_view, 0)
            }
        }
    }

    private val emptyViewAdapterObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            modifyEmptyView()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            modifyEmptyView()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            modifyEmptyView()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        this.adapter?.let {
            if (it.hasObservers()) it.unregisterAdapterDataObserver(emptyViewAdapterObserver)
        }

        super.setAdapter(adapter)

        adapter?.registerAdapterDataObserver(emptyViewAdapterObserver)

        if (isAdapterEmpty()) modifyEmptyView()
    }

    fun setOnlyOneAdapter(adapter: Adapter<*>) {
        if (this.adapter == null) setAdapter(adapter)
    }

    private fun modifyEmptyView() {
        if (emptyViewResId != 0) {
            (parent as? View?)?.findViewById<View>(emptyViewResId)?.let {
                val showEmptyView = isAdapterEmpty()
                isVisible = !showEmptyView
                if (it is ContentLoadingProgressBar) {
                    if (showEmptyView) {
                        it.show()
                    } else {
                        it.hide()
                    }
                } else {
                    it.isVisible = showEmptyView
                }
            }
        }
    }

    private fun isAdapterEmpty() = (this.adapter?.itemCount ?: 0) == 0

    // 修复ViewPager2滑动冲突
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (fitViewPager) {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.x
                    startY = ev.y
                    parent?.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    val endX = ev.x
                    val endY = ev.y
                    val disX = abs(endX - startX)
                    val disY = abs(endY - startY)
                    if (disX > disY) {
                        parent?.requestDisallowInterceptTouchEvent(canScrollHorizontally((startX - endX).toInt()))
                    } else {
                        parent?.requestDisallowInterceptTouchEvent(canScrollVertically((startY - endY).toInt()))
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}