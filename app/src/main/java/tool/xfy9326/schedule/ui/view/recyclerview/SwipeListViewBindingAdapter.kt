package tool.xfy9326.schedule.ui.view.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import tool.xfy9326.schedule.databinding.LayoutSwipeItemBinding

abstract class SwipeListViewBindingAdapter<E : Any, V : ViewBinding>(callBack: DiffUtil.ItemCallback<E>? = null) :
    ListViewBindingAdapter<E, LayoutSwipeItemBinding, SwipeItemViewHolder<V>>(callBack) {

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        ItemTouchHelper(SwipeItemCallback().apply {
            setOnItemSwipedListener(::onSwipe)
        }).attachToRecyclerView(recyclerView)
    }

    final override fun onCreateViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): SwipeItemViewHolder<V> {
        return LayoutSwipeItemBinding.inflate(layoutInflater, parent, false).run {
            onCreateContentViewBinding(layoutInflater, layoutSwipeForeground, viewType).let {
                layoutSwipeForeground.addView(it.root)
                SwipeItemViewHolder(it, this)
            }
        }
    }

    final override fun onBindViewHolder(holder: SwipeItemViewHolder<V>, position: Int, element: E) =
        onBindViewHolder(holder.viewContext, holder.binding, position, element)

    abstract fun onCreateContentViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): V

    abstract fun onBindViewHolder(viewContext: Context, binding: V, position: Int, element: E)

    private fun onSwipe(position: Int) {
        onItemSwiped(position, getItem(position))
    }

    protected abstract fun onItemSwiped(position: Int, element: E)
}