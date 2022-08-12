package tool.xfy9326.schedule.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.children
import io.github.xfy9326.atools.base.isEven
import io.github.xfy9326.atools.base.isOdd
import tool.xfy9326.schedule.R
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import tool.xfy9326.schedule.content.utils.hasCourse

class WeekNumEditView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    AutoSquareLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        fun createCircleNumberButton(context: Context, num: Int, checked: Boolean) =
            CircleNumberButton(context).apply {
                showNum = num
                isChecked = checked
            }
    }

    init {
        childViewSize = resources.getDimensionPixelSize(R.dimen.circle_number_button_size)
        childViewMinMargin = resources.getDimensionPixelSize(R.dimen.circle_number_button_min_margin)
    }

    fun setWeekNum(weekNumArray: BooleanArray, maxWeekNum: Int) {
        removeAllViewsInLayout()

        repeat(maxWeekNum) {
            val weekNum = it + 1
            innerAddView(createCircleNumberButton(context, weekNum, weekNumArray.hasCourse(weekNum)))
        }

        requestLayout()
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

    override fun onMeasureColumnAmount(suggestColumnAmount: Int): Int = when {
        suggestColumnAmount <= 1 -> 1
        suggestColumnAmount.isOdd() -> suggestColumnAmount - 1
        else -> suggestColumnAmount
    }

    override fun addView(child: View?, index: Int, params: LayoutParams?) {}
}