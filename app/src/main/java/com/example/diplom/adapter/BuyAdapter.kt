package com.example.diplom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.ItemHistoryOrderLayoutBinding

class BuyAdapter(private val context: Context, private val buyList: List<Buy>) :
    RecyclerView.Adapter<BuyAdapter.BuyViewHolder>(){

        inner class BuyViewHolder(private val binding: ItemHistoryOrderLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
            val productImage = binding.productImageView
            val titleEng = binding.titleEngTextView
            val productId = binding.productIdTextView
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyViewHolder {
        val binding = ItemHistoryOrderLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuyViewHolder, position: Int) {
        val buy = buyList[position]
        holder.titleEng.text = buy.TitleEng
        holder.productId.text = buy.ProductId.toString()

        val resourceId = context.resources.getIdentifier(buy.Image, "drawable", context.packageName)
        if(resourceId != 0){
            holder.productImage.setImageResource(resourceId)
        }
    }

    override fun getItemCount(): Int {
        return buyList.size
    }
}