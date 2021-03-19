package tool.xfy9326.schedule.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import tool.xfy9326.schedule.content.utils.hasCourse
import tool.xfy9326.schedule.kt.isEven
import tool.xfy9326.schedule.kt.isOdd
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class WeekNumEditView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        fun createCircleNumberButton(context: Context, num: Int, checked: Boolean) =
            CircleNumberButton(context).apply {
                showNum = num
                isChecked = checked
            }
    }

    private var columnAmount = 1
    private var rowAmount = 1
    private var weekNumArray = BooleanArray(0)
    private var maxWeekNum = 1

    private val weekNumCellSize = resources.getDimensionPixelSize(R.dimen.circle_number_button_size)
    private val weekNumCellMargin = resources.getDimensionPixelSize(R.dimen.circle_number_button_margin)
    private val fullWeekNumCellSize = weekNumCellSize + weekNumCellMargin * 2
    private val weekNumCellMeasureSpec = MeasureSpec.makeMeasureSpec(weekNumCellSize, MeasureSpec.EXACTLY)

    fun setWeekNum(weekNumArray: BooleanArray, maxWeekNum: Int) {
        if (this.maxWeekNum != maxWeekNum || !this.weekNumArray.contentEquals(weekNumArray)) {
            this.weekNumArray = weekNumArray
            this.maxWeekNum = maxWeekNum

            removeAllViewsInLayout()

            repeat(maxWeekNum) {
                val weekNum = it + 1
                addViewInLayout(
                    createCircleNumberButton(context, weekNum, weekNumArray.hasCourse(weekNum)),
                    -1,
                    LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT),
                    true
                )
            }

            requestLayout()
        }
    }

    fun getWeekNumArray() =
        children.filterIsInstance<CircleNumberButton>().map {
            it.isChecked
        }.toList().toBooleanArray().arrangeWeekNum()

    fun checkAllOddWeekNum() {
        for (child in children) {
            child as CircleNumberButton
            child.isChecked = child.showNum.isOdd()
        }
    }

    fun checkAllEvenWeekNum() {
        for (child in children) {
            child as CircleNumberButton
            child.isChecked = child.showNum.isEven()
        }
    }

    fun checkAllOppositeWeekNum() {
        for (child in children) {
            child as CircleNumberButton
            child.isChecked = !child.isChecked
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val column = floor(1f * width / fullWeekNumCellSize).toInt()
        columnAmount = when {
            column <= 1 -> 1
            column.isOdd() -> column - 1
            else -> column
        }
        rowAmount = ceil(1f * maxWeekNum / columnAmount).toInt()

        for (child in children) {
            child.measure(weekNumCellMeasureSpec, weekNumCellMeasureSpec)
        }

        val viewHeight = fullWeekNumCellSize * rowAmount

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val centerOffset = (((1f * width / columnAmount) - fullWeekNumCellSize) / 2f).roundToInt()
        val leftToRight = layoutDirection == LAYOUT_DIRECTION_LTR
        var childLeft = getCellStartX(leftToRight, width) + getCellXOffset(true, leftToRight, centerOffset)
        var childTop = getCellYOffset(true)

        for ((i, child) in children.withIndex()) {
            if (i != 0 && i % columnAmount == 0) {
                childLeft = getCellStartX(leftToRight, width) + getCellXOffset(true, leftToRight, centerOffset)
                childTop += getCellYOffset(false)
            }

            child.layout(childLeft, childTop, childLeft + weekNumCellSize, childTop + weekNumCellSize)

            childLeft += getCellXOffset(false, leftToRight, centerOffset)
        }
    }

    private fun getCellStartX(leftToRight: Boolean, width: Int) = if (leftToRight) 0 else width

    private fun getCellXOffset(startColumn: Boolean, leftToRight: Boolean, centerOffset: Int) =
        if (leftToRight) {
            if (startColumn) {
                weekNumCellMargin + centerOffset
            } else {
                fullWeekNumCellSize + centerOffset * 2
            }
        } else {
            if (startColumn) {
                -weekNumCellSize - weekNumCellMargin - centerOffset
            } else {
                -fullWeekNumCellSize - centerOffset * 2
            }
        }

    private fun getCellYOffset(startRow: Boolean) =
        if (startRow) weekNumCellMargin else fullWeekNumCellSize

    override fun addView(child: View?, index: Int, params: LayoutParams?) {}
}