package tool.xfy9326.schedule.ui.view.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.ui.viewholder.ViewBindingViewHolder

abstract class BaseViewBindingAdapter<V : ViewBinding, VH : ViewBindingViewHolder<V>>
    : RecyclerView.Adapter<VH>(), IViewBindingAdapter<V, VH> {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        onCreateViewHolder(LayoutInflater.from(parent.context), parent, viewType)
}