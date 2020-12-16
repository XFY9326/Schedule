@file:Suppress("unused")

package tool.xfy9326.schedule.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.widget.AppCompatCheckedTextView
import tool.xfy9326.schedule.R

class CircleNumberButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.checkedTextViewStyle,
) : AppCompatCheckedTextView(context, attrs, defStyleAttr), View.OnClickListener {
    var showNum: Int = 0
        set(value) {
            field = value
            text = showNum.toString()
        }

    private var checkedListener: ((CircleNumberButton, Boolean) -> Unit)? = null

    init {
        elevation = resources.getDimension(R.dimen.circle_number_button_elevation)
        gravity = Gravity.CENTER
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setBackgroundResource(R.drawable.background_circple_number_button)
        setTextColor(Color.WHITE)
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        toggle()
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        checkedListener?.invoke(this, checked)
    }

    fun setOnCheckedChangeListener(listener: ((CircleNumberButton, Boolean) -> Unit)?) {
        this.checkedListener = listener
    }
}