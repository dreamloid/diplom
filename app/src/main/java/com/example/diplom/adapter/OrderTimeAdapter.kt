package com.example.diplom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.R
import com.example.diplom.databinding.ItemOrderTimeDeliveryLayoutBinding

class OrderTimeAdapter(private val context: Context, val timeList: List<OrderTime>) :
    RecyclerView.Adapter<OrderTimeAdapter.OrderTimeViewHolder>(){

    interface OnItemClickListener {
        fun onItemClick(orderTime: OrderTime)
    }

    private var listener: OnItemClickListener? = null
    var selectedItemPosition: Int? = null

    // Обновление выбранного элемента
    fun updateSelectedItem(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged()
    }

    inner class OrderTimeViewHolder(private val binding: ItemOrderTimeDeliveryLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val timeIdTextView = binding.timeIdTextView
        val dayTextView = binding.dayTextView
        val timeTextView = binding.timeTextView
        val frameLayout = binding.frameLayout

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectedItemPosition = position
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderTimeViewHolder {
        val binding = ItemOrderTimeDeliveryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderTimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderTimeViewHolder, position: Int) {
        val orderTime = timeList[position]
        holder.timeIdTextView.text = orderTime.time_id.toString()
        holder.dayTextView.text = orderTime.day
        holder.timeTextView.text = orderTime.time

        // Установка drawable
        if (position == selectedItemPosition) {
            holder.frameLayout.setBackgroundResource(R.drawable.frame_rectangle10_darkgreen)
        } else {
            holder.frameLayout.setBackgroundResource(R.drawable.frame_rectangle10_dirtygreen)
        }
    }

    override fun getItemCount(): Int {
        return timeList.size
    }
}