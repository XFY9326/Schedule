package tool.xfy9326.schedule.ui.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt


class AdvancedDividerItemDecoration(context: Context, private var orientation: Int) : DividerItemDecoration(context, orientation) {
    private val mBounds = Rect()

    private var horizonMargins = 0

    override fun setOrientation(orientation: Int) {
        super.setOrientation(orientation)
        this.orientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val drawable = this.drawable
        if (parent.layoutManager == null || parent.layoutManager !is LinearLayoutManager || drawable == null) return
        if (orientation == VERTICAL) {
            drawVertical(c, parent, drawable, state)
        } else if (orientation == HORIZONTAL) {
            drawHorizontal(c, parent, drawable, state)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView, drawable: Drawable, state: RecyclerView.State) {
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

        val childCount = state.itemCount - 1
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + child.translationY.roundToInt()
            val top = bottom - drawable.intrinsicHeight
            drawable.setBounds(left + horizonMargins, top, right - horizonMargins, bottom)
            drawable.draw(canvas)
        }
        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView, drawable: Drawable, state: RecyclerView.State) {
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

        val childCount = state.itemCount - 1
        val layoutManager = parent.layoutManager!!
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            layoutManager.getDecoratedBoundsWithMargins(child, mBounds)
            val right = mBounds.right + child.translationX.roundToInt()
            val left = right - drawable.intrinsicWidth
            drawable.setBounds(left, top + horizonMargins, right, bottom - horizonMargins)
            drawable.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == state.itemCount - 1) {
            outRect.setEmpty()
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
}