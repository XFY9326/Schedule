package tool.xfy9326.schedule.ui.view.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import lib.xfy9326.kit.NEW_LINE
import tool.xfy9326.schedule.beans.SchedulePredefine
import tool.xfy9326.schedule.beans.ScheduleStyles
import tool.xfy9326.schedule.beans.ScheduleText
import tool.xfy9326.schedule.beans.ScheduleTime
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.endTimeStr
import tool.xfy9326.schedule.beans.ScheduleTime.Companion.startTimeStr

@SuppressLint("ViewConstructor")
class ScheduleTimeCellView(
    context: Context,
    private val index: Int,
    private val scheduleTime: ScheduleTime,
    private val predefine: SchedulePredefine,
    private val styles: ScheduleStyles,
) : LinearLayoutCompat(context), IScheduleCell {
    override val column: Int = 0
    override val row: Int = index
    override val rowSpan: Int = 1

    init {
        alpha = styles.scheduleViewAlpha
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(predefine.gridCellPadding)
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        initView()
    }

    private fun initView() {
        val courseTimeNumText = (index + 1).toString()
        val timeTextColor = styles.getTimeTextColor(context)

        addViewPreventLayout(
            TextView(context).apply {
                text = courseTimeNumText
                textSize = styles.textSize[ScheduleText.SCHEDULE_NUMBER_TEXT]
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                setTextColor(timeTextColor)
                setPadding(0, predefine.timeCellVerticalPadding, 0, predefine.timeCellVerticalPadding)

                textAlignment = View.TEXT_ALIGNMENT_INHERIT
                gravity = Gravity.CENTER

                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false
            }
        )

        if (styles.showScheduleTimes) {
            val courseTimeText = scheduleTime.startTimeStr + NEW_LINE + scheduleTime.endTimeStr
            addViewPreventLayout(
                TextView(context).apply {
                    text = courseTimeText
                    textSize = styles.textSize[ScheduleText.SCHEDULE_TIME_TEXT]
                    setTextColor(timeTextColor)
                    setPadding(0, predefine.timeCellTimeDivideTopMargin, 0, 0)

                    textAlignment = View.TEXT_ALIGNMENT_INHERIT
                    gravity = Gravity.CENTER

                    layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(0, predefine.timeCellTimeDivideTopMargin, 0, 0)
                    }

                    isHorizontalScrollBarEnabled = false
                    isVerticalScrollBarEnabled = false
                }
            )
        }
    }

    private fun addViewPreventLayout(view: View) = addViewInLayout(view, -1, view.layoutParams, true)
}