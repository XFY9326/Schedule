package tool.xfy9326.schedule.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tool.xfy9326.schedule.ui.fragment.TableFragment

class ScheduleViewPagerAdapter(context: FragmentActivity, private var maxWeekNum: Int) : FragmentStateAdapter(context) {

    fun updateMaxWeekNum(maxWeekNum: Int) {
        if (this.maxWeekNum != maxWeekNum) {
            this.maxWeekNum = maxWeekNum
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = maxWeekNum

    override fun createFragment(position: Int): Fragment = TableFragment.create(position + 1)
}