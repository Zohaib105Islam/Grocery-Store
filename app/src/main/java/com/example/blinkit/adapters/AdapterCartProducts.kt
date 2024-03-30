package com.example.blinkit.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.blinkit.databinding.ItemViewCartProductsBinding
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.viewmodels.UserViewModel

class AdapterCartProducts() : RecyclerView.Adapter<AdapterCartProducts.CartProductViewHolder>() {
    class CartProductViewHolder(val binding: ItemViewCartProductsBinding) : ViewHolder(binding.root){}



    val diffUtil= object : DiffUtil.ItemCallback<CartProducts>(){
        override fun areItemsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem.itemPushKey == newItem.itemPushKey
        }

        override fun areContentsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem == newItem
        }

    }

    val differ=AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        return CartProductViewHolder(ItemViewCartProductsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size

    }

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val product=differ.currentList[position]

        holder.binding.apply {
            Glide.with(holder.itemView).load(product.productImage).into(ivProductImage)
            tvProductQuantity.text=product.productQuantity
            tvProductTitleCart.text=product.productTitle
            tvProductPriceCart.text=product.productPrice
            tvProductCount.text=product.productCount.toString()

        }
    }
}