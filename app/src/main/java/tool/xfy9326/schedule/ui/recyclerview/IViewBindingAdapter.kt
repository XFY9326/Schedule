package tool.xfy9326.schedule.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

interface IViewBindingAdapter<V : ViewBinding, VH : ViewBindingViewHolder<out V>> {
    fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): VH
}