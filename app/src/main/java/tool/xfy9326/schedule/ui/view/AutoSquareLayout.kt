package tool.xfy9326.schedule.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.children
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

open class AutoSquareLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    @Px
    var childViewSize = 0
        set(value) {
            field = value
            requestLayout()
        }

    @Px
    var childViewMinMargin = 0
        set(value) {
            field = value
            requestLayout()
        }

    private var columnAmount = 1
    private var rowAmount = 1

    protected open fun onMeasureColumnAmount(suggestColumnAmount: Int): Int = suggestColumnAmount

    final override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childViewSize > 0 && childCount > 0) {
            val childMinSize = childViewSize + childViewMinMargin * 2

            val viewMeasuredWidth: Int
            val width = MeasureSpec.getSize(widthMeasureSpec)
            when (MeasureSpec.getMode(widthMeasureSpec)) {
                MeasureSpec.UNSPECIFIED -> {
                    columnAmount = onMeasureColumnAmount(childCount)
                    rowAmount = 1
                    viewMeasuredWidth = (childCount * childMinSize).coerceAtLeast(suggestedMinimumWidth)
                }
                MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> {
                    columnAmount = onMeasureColumnAmount((1f * width / childMinSize).toInt())
                    rowAmount = ceil(1f * childCount / columnAmount).toInt()
                    viewMeasuredWidth = MeasureSpec.getSize(widthMeasureSpec).coerceAtLeast(suggestedMinimumWidth)
                }
                else -> error("Unknown MeasureSpec Type!")
            }
            val viewExceptHeight = rowAmount * childMinSize

            val viewMeasuredHeight =
                when (MeasureSpec.getMode(heightMeasureSpec)) {
                    MeasureSpec.UNSPECIFIED -> viewExceptHeight
                    MeasureSpec.AT_MOST -> min(viewExceptHeight, MeasureSpec.getSize(heightMeasureSpec))
                    MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
                    else -> error("Unknown MeasureSpec Type!")
                }.coerceAtLeast(suggestedMinimumHeight)

            val childViewMeasureSpec = MeasureSpec.makeMeasureSpec(childViewSize, MeasureSpec.EXACTLY)
            for (child in children) {
                child.measure(childViewMeasureSpec, childViewMeasureSpec)
            }

            setMeasuredDimension(viewMeasuredWidth, viewMeasuredHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    final override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            val width = r - l
            if (width > 0 && childCount > 0) {
                val xCenterOffset = ((1f * width / columnAmount - childViewSize) / 2f).roundToInt()
                val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR
                var childLeft = getCellStartX(leftToRight, width) + getCellXOffset(true, leftToRight, xCenterOffset)
                var childTop = getCellYOffset(true)

                for ((i, child) in children.withIndex()) {
                    if (i != 0 && i % columnAmount == 0) {
                        childLeft = getCellStartX(leftToRight, width) + getCellXOffset(true, leftToRight, xCenterOffset)
                        childTop += getCellYOffset(false)
                    }

                    child.layout(childLeft, childTop, childLeft + childViewSize, childTop + childViewSize)

                    childLeft += getCellXOffset(false, leftToRight, xCenterOffset)
                }
            }
        }
    }

    private fun getCellStartX(leftToRight: Boolean, width: Int) = if (leftToRight) 0 else width

    private fun getCellXOffset(startColumn: Boolean, leftToRight: Boolean, centerOffset: Int) =
        if (leftToRight) {
            if (startColumn) {
                centerOffset
            } else {
                childViewSize + centerOffset * 2
            }
        } else {
            if (startColumn) {
                -childViewSize - centerOffset
            } else {
                -childViewSize - centerOffset * 2
            }
        }

    private fun getCellYOffset(startRow: Boolean) =
        if (startRow) childViewMinMargin else childViewSize + childViewMinMargin * 2

    protected fun innerAddView(view: View) =
        addViewInLayout(view, -1, view.layoutParams ?: LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT), true)
}