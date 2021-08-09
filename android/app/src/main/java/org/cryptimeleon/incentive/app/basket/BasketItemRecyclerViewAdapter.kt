package org.cryptimeleon.incentive.app.basket

import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.cryptimeleon.incentive.app.databinding.ViewBasketItemBinding
import org.cryptimeleon.incentive.app.network.Item
import java.util.*

class BasketItemRecyclerViewAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<BasketItemRecyclerViewAdapter.BasketListItem, BasketItemRecyclerViewAdapter.ViewHolder>(
        DiffCallback
    ) {

    class ViewHolder(val binding: ViewBasketItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: BasketListItem) {
            binding.basketListItem = data
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BasketListItem>() {
        override fun areItemsTheSame(oldItem: BasketListItem, newItem: BasketListItem): Boolean {
            return oldItem.item.id == newItem.item.id
        }

        override fun areContentsTheSame(oldItem: BasketListItem, newItem: BasketListItem): Boolean {
            // TODO better check possible?
            return oldItem.count == newItem.count && oldItem.item == newItem.item
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            (ViewBasketItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ))
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val basketItem = getItem(position)
        holder.bind(basketItem)
        holder.binding.buttonAdd.setOnClickListener { onClickListener.onAdd(basketItem) }
        holder.binding.buttonRemoveNew.setOnClickListener { onClickListener.onRemove(basketItem) }
    }

    /**
     * Simple class for data binding
     */
    class BasketListItem(val item: Item, val count: Int) {
        private val locale = Locale.GERMANY
        private val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        val priceSingle: String = currencyFormat.format(item.price / 100.0)
        val priceTotal: String = currencyFormat.format(item.price * count / 100.0)
        val countStr: String = count.toString()
    }

    class OnClickListener(
        val onAddListener: (basketListItem: BasketListItem) -> Unit,
        val onRemoveListener: (basketListItem: BasketListItem) -> Unit
    ) {
        fun onAdd(basketListItem: BasketListItem) = onAddListener(basketListItem)
        fun onRemove(basketListItem: BasketListItem) = onRemoveListener(basketListItem)
    }
}

/*constrain layout align to the right*
 * This adds a option to the RecyclerView xml
 */
@BindingAdapter("listData")
fun bindRecyclerView(
    recyclerView: RecyclerView,
    data: List<BasketItemRecyclerViewAdapter.BasketListItem>?
) {
    val adapter = recyclerView.adapter as BasketItemRecyclerViewAdapter
    adapter.submitList(data)
}


