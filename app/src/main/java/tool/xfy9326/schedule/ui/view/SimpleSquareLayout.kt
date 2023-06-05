package tool.xfy9326.schedule.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.max

class SimpleSquareLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val maxSize = max(measuredWidth, measuredHeight)
        setMeasuredDimension(maxSize, maxSize)
    }
}