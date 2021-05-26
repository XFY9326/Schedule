package tool.xfy9326.schedule.ui.view.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

abstract class ListViewBindingAdapter<E, V : ViewBinding, VH : ViewBindingViewHolder<out V>>(callBack: DiffUtil.ItemCallback<E>? = null) :
    ListAdapter<E, VH>(callBack ?: BaseDifferItemCallBack<E>()), IViewBindingAdapter<V, VH> {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        onCreateViewHolder(LayoutInflater.from(parent.context), parent, viewType)

    override fun onBindViewHolder(holder: VH, position: Int) = onBindViewHolder(holder, position, getItem(position))

    protected open fun onBindViewHolder(holder: VH, position: Int, element: E) {}
}