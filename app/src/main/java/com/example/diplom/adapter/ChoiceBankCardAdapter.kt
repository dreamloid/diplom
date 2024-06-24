package com.example.diplom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.OrderRegistrationActivity
import com.example.diplom.databinding.ItemChoiceBankcardBinding

class ChoiceBankCardAdapter(private val context: Context, private val bankcardList: MutableList<BankCard>): RecyclerView.Adapter<ChoiceBankCardAdapter.ChoiceBankCardViewHolder>() {

    private var selectedNumberTextView: TextView? = null
    private var selectedImageView: ImageView? = null
    private var choiceBankCardFrameLayout: FrameLayout? = null
    private var selectedBankCardFrameLayout: FrameLayout? = null
    private var choiceBankCardTextView: TextView? = null


    inner class ChoiceBankCardViewHolder(private val binding: ItemChoiceBankcardBinding): RecyclerView.ViewHolder(binding.root){
        val systemImageView = binding.systemImageView
        val numbersBankCardTextView = binding.numbersCardTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceBankCardAdapter.ChoiceBankCardViewHolder {
        val binding = ItemChoiceBankcardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChoiceBankCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChoiceBankCardAdapter.ChoiceBankCardViewHolder, position: Int) {
//        val bankCard = bankcardList[position]
//        holder.numbersBankCardTextView.text = bankCard.Number
//
//        val systemImageId = context.resources.getIdentifier(bankCard.SystemBank, "drawable", context.packageName)
//        if(systemImageId != 0){
//            holder.systemImageView.setImageResource(systemImageId)
//            }
//
//        holder.itemView.setOnClickListener {
//            selectedNumberTextView?.text = bankCard.Number
//            selectedImageView?.setImageResource(systemImageId)
//            choiceBankCardFrameLayout?.visibility = View.GONE
//            selectedBankCardFrameLayout?.visibility = View.VISIBLE
//            choiceBankCardTextView?.visibility = View.GONE
//        }
    }

    override fun getItemCount(): Int {
        return bankcardList.size
    }

    fun setSelectedNumberTextView(textView: TextView) {
        selectedNumberTextView = textView
    }

    fun setSelectedImageView(imageView: ImageView) {
        selectedImageView = imageView
    }

    fun setChoiceBankCardFrameLayout(frameLayout: FrameLayout) {
        choiceBankCardFrameLayout = frameLayout
    }

    fun setSelectedBankCardFrameLayout(frameLayout: FrameLayout) {
        selectedBankCardFrameLayout = frameLayout
    }

    fun setChoiceBankCardTextView(textView: TextView) {
        choiceBankCardTextView = textView
    }
}