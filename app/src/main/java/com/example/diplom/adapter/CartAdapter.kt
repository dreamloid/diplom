package com.example.diplom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.DatabaseHelper
import com.example.diplom.databinding.ItemCartLayoutBinding
import com.example.diplom.db.UserManager

class CartAdapter(private val context: Context, private var cartList: List<Similar>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>(){

    private var onProductClickListener: OnProductClickListener? = null

        inner class CartViewHolder(private val binding: ItemCartLayoutBinding) : RecyclerView.ViewHolder(binding.root){
            val cartImageView = binding.imageViewProduct
            val titleTextView = binding.textViewTitle
            val volumeTextView = binding.textViewProductVolume
            val priceTextView = binding.textViewPrice
            val titleEngTextView = binding.textViewTitleEng
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cart = cartList[position]
        holder.titleTextView.text = cart.Title
        holder.volumeTextView.text = cart.Volume
        holder.priceTextView.text = cart.Price
        holder.titleEngTextView.text = cart.TitleEng

        val resourceId = context.resources.getIdentifier(cart.Image, "drawable", context.packageName)
        if(resourceId != 0){
            holder.cartImageView.setImageResource(resourceId)
        }

        holder.itemView.setOnClickListener{
            val productId = cartList[position].productId
            val categoryId = cartList[position].categoryId
            onProductClickListener?.onProductClick(productId, categoryId)
        }
    }


    override fun getItemCount(): Int {
        return cartList.size
    }

    interface OnProductClickListener {
        fun onProductClick(productId: Int, categoryId: Int)
    }

    fun setOnProductClickListener(listener: OnProductClickListener) {
        this.onProductClickListener = listener
    }
}