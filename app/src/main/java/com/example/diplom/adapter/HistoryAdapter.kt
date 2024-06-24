package com.example.diplom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.FragmentCartBinding
import com.example.diplom.databinding.ItemHistoryLayoutBinding

class HistoryAdapter(private val context: Context, private val historyList: List<History>, private val itemClickListener: OnItemClickListener, private val buttonClickListener: onButtonClickListener) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(){

    interface OnItemClickListener {
        fun onItemClick(orderId: Int)
    }

    interface onButtonClickListener{
        fun onButtonClick(orderId: Int)
    }

        inner class HistoryViewHolder(private val binding: ItemHistoryLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
            val dateAndTime = binding.dateAndTimeTextView

            val frameLayout: ConstraintLayout = binding.frameLayout
            val sendReviewButton: Button = binding.sendReviewButton

            init {
                frameLayout.setOnClickListener{
                //itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val history = historyList[position]
                        itemClickListener.onItemClick(history.orderId)
                    }
                }
                sendReviewButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val history = historyList[position]
                        buttonClickListener.onButtonClick(history.orderId)
                    }
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]
        holder.dateAndTime.text = history.DateAndTime
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}