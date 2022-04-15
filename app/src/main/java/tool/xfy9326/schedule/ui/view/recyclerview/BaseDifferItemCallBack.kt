@file:Suppress("DiffUtilEquals")

package tool.xfy9326.schedule.ui.view.recyclerview

import androidx.recyclerview.widget.DiffUtil

open class BaseDifferItemCallBack<E : Any> : DiffUtil.ItemCallback<E>() {
    override fun areContentsTheSame(oldItem: E, newItem: E): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: E, newItem: E): Boolean {
        return oldItem === newItem
    }
}