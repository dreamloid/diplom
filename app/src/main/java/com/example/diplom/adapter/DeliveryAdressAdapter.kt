package com.example.diplom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.ItemDeliveryadressLayoutBinding

class DeliveryAdressAdapter(private val context: Context, private val deliveryList: List<DeliveryAdress>):
    RecyclerView.Adapter<DeliveryAdressAdapter.DeliveryViewHolder>(){

        private var choiceDeliveryTextView: TextView? = null
        private var selectedChoiceDeliveryFrameLayout: FrameLayout? = null
        private var selectedChoiceDeliveryTextView: TextView? = null
        private var choiceDeliveryFrameLayout: FrameLayout? = null

        // Интерфейс для обработки нажатий на элементы RecyclerView
        interface OnItemClickListener {
            fun onItemClick(deliveryAdress: DeliveryAdress)
        }

        private var listener: OnItemClickListener? = null

        // Установка слушателя
        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.listener = listener
        }

        inner class DeliveryViewHolder(private val binding: ItemDeliveryadressLayoutBinding): RecyclerView.ViewHolder(binding.root) {
            val deliveryAdress = binding.deliveryAdressTextView

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val deliveryAdress = deliveryList[position]
                        listener?.onItemClick(deliveryAdress)
                        selectedChoiceDeliveryTextView?.text = "${deliveryAdress.street} д. ${deliveryAdress.house} кв. ${deliveryAdress.flat}"
                        onItemClickListener?.invoke(deliveryAdress.delivery_id)
                        choiceDeliveryTextView?.visibility = View.GONE
                        selectedChoiceDeliveryFrameLayout?.visibility = View.VISIBLE
                        choiceDeliveryFrameLayout?.visibility = View.GONE
                    }
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val binding = ItemDeliveryadressLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        val delivery = deliveryList[position]
        holder.deliveryAdress.text = "${delivery.street} д. ${delivery.house} кв. ${delivery.flat}"
    }

    override fun getItemCount(): Int {
        return deliveryList.size
    }

    // Добавляем слушатель для обработки нажатий
    private var onItemClickListener: ((Int) -> Unit)? = null

    //поменять текст
    fun setSelectedChoiceOverTextView(textView: TextView){
        selectedChoiceDeliveryTextView = textView
    }
    //убрать текстВью
    fun setChoiceDeliveryTextView(textView: TextView){
        choiceDeliveryTextView = textView
    }
    //показать фрейм с выбранным
    fun setSelectedChoiceDeliveryFrameLayout(frameLayout: FrameLayout) {
        selectedChoiceDeliveryFrameLayout = frameLayout
    }
    //убрать фрейм с выбором
    fun setChoiceDeliveryFrameLayout(frameLayout: FrameLayout){
        choiceDeliveryFrameLayout = frameLayout
    }
}