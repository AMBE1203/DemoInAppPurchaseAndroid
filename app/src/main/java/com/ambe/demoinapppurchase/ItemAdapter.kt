package com.ambe.demoinapppurchase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails

class ItemAdapter(val onClickItem: (Int) -> Unit) :
  ListAdapter<ProductDetails, ItemAdapter.ItemViewHolder>(ItemDiffCallBack()) {

  class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val name: TextView = itemView.findViewById(R.id.txt_name)
    fun bindData(product: ProductDetails, onClickItem: (Int) -> Unit) {
      name.text = "${product.name} - ${product.title}"
      name.setOnClickListener { onClickItem(adapterPosition) }
    }

  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return ItemViewHolder(inflater.inflate(R.layout.item, parent, false))
  }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    holder.bindData(getItem(position), onClickItem)
  }
}

class ItemDiffCallBack : DiffUtil.ItemCallback<ProductDetails>() {
  override fun areItemsTheSame(oldItem: ProductDetails, newItem: ProductDetails): Boolean {
    return oldItem.productId == newItem.productId
  }

  override fun areContentsTheSame(oldItem: ProductDetails, newItem: ProductDetails): Boolean {
    return oldItem == newItem
  }

}