package com.example.diplom.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.ProductFragment
import com.example.diplom.R
import com.example.diplom.databinding.ItemSimilarLayoutBinding

class SimilarAdapter(private val context: Context, private val similarList: List<Similar>) :
    RecyclerView.Adapter<SimilarAdapter.SimilarViewHolder>(){

    private lateinit var productList: List<Product>
    private var onProductClickListener: SimilarAdapter.OnProductClickListener? = null

    inner class SimilarViewHolder(private  val binding: ItemSimilarLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val similarImageView = binding.imageViewProduct
        val titleTextView = binding.textViewTitle
        val volumeTextView = binding.textViewProductVolume
        val priceTextView = binding.textViewPrice
        val titleEngTextView = binding.textViewTitleEng
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarAdapter.SimilarViewHolder {
        val binding = ItemSimilarLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimilarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimilarAdapter.SimilarViewHolder, position: Int) {
        val similar = similarList[position]
        holder.titleTextView.text = similar.Title
        holder.volumeTextView.text = similar.Volume
        holder.priceTextView.text = similar.Price
        holder.titleEngTextView.text = similar.TitleEng

        val resourceId = context.resources.getIdentifier(similar.Image, "drawable", context.packageName)
        if(resourceId != 0){
            holder.similarImageView.setImageResource(resourceId)
        }

        holder.itemView.setOnClickListener{
            val productId = similarList[position].productId
            val categoryId = similarList[position].categoryId
            onProductClickListener?.onProductClick(productId, categoryId)
        }
    }

    override fun getItemCount(): Int {
        return similarList.size
    }

    interface OnProductClickListener {
        fun onProductClick(productId: Int, categoryId: Int)
    }

    fun setOnProductClickListener(listener: OnProductClickListener) {
        this.onProductClickListener = listener
    }
}