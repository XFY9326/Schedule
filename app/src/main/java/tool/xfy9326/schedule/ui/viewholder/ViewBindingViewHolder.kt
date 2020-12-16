@file:Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")

package tool.xfy9326.schedule.ui.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class ViewBindingViewHolder<V : ViewBinding>(val viewBinding: V) : RecyclerView.ViewHolder(viewBinding.root) {
    val viewContext: Context
        get() = viewBinding.root.context
}