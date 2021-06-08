package tool.xfy9326.schedule.ui.view.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt


class AdvancedDividerItemDecoration(context: Context, private var orientation: Int, private val horizonMargins: Int = 0) :
    DividerItemDecoration(context, orientation) {
    private val mBounds = Rect()
    private var dividerDrawable: Drawable? = null
    private var drawDividerListener: ((position: Int, itemAmount: Int) -> Boolean)? = { position, itemAmount ->
        position != itemAmount - 1
    }

    fun setDividerDrawable(drawable: Drawable) {
        dividerDrawable = drawable
    }

    fun setOnDrawDividerListener(drawDividerListener: ((position: Int, itemAmount: Int) -> Boolean)?) {
        this.drawDividerListener = drawDividerListener
    }

    override fun setOrientation(orientation: Int) {
        super.setOrientation(orientation)
        this.orientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val divider = dividerDrawable ?: drawable
        if (parent.layoutManager == null || parent.layoutManager !is LinearLayoutManager || divider == null) return
        if (orientation == VERTICAL) {
            drawVertical(c, parent, divider)
        } else if (orientation == HORIZONTAL) {
            drawHorizontal(c, parent, divider)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView, drawable: Drawable) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        val listener = drawDividerListener
        val childCount = parent.childCount
        val layoutManager = parent.layoutManager!!
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            if (listener != null && !listener(parent.getChildAdapterPosition(child), layoutManager.itemCount)) continue

            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + child.translationY.roundToInt()
            val top = bottom - drawable.intrinsicHeight
            drawable.setBounds(left + horizonMargins, top, right - horizonMargins, bottom)
            drawable.draw(canvas)
        }
        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView, drawable: Drawable) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
        } else {
            top = 0
            bottom = parent.height
        }

        val listener = drawDividerListener
        val childCount = parent.childCount
        val layoutManager = parent.layoutManager!!
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            if (listener != null && !listener(parent.getChildAdapterPosition(child), layoutManager.itemCount)) continue

            layoutManager.getDecoratedBoundsWithMargins(child, mBounds)
            val right = mBounds.right + child.translationX.roundToInt()
            val left = right - drawable.intrinsicWidth
            drawable.setBounds(left, top + horizonMargins, right, bottom - horizonMargins)
            drawable.draw(canvas)
        }
        canvas.restore()
    }
}