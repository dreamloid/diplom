package com.example.diplom.adapter

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.R
import com.example.diplom.ReviewActivity
import com.example.diplom.databinding.ItemChoiceReviewLayoutBinding

class ChoiceReviewAdapter(private val context: Context, private val choiceList: List<ChoiceReview>):
    RecyclerView.Adapter<ChoiceReviewAdapter.ChoiceViewHolder>(){

    private val selectedStarsList: MutableList<Int> = MutableList(choiceList.size) { 0 }
    private var selectedStar: Int = 0 // добавляем переменную как член класса

        inner class ChoiceViewHolder(private val binding: ItemChoiceReviewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
            val productImage = binding.productImageView
            val title = binding.titleTextView
            val productId = binding.productIdTextView

            val starImageViews = arrayOf(
                binding.star1ImageView,
                binding.star2ImageView,
                binding.star3ImageView,
                binding.star4ImageView,
                binding.star5ImageView
            )

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val clickedItem = choiceList[position]

                        selectedStar = selectedStarsList[position]

                        val intent = Intent(context, ReviewActivity::class.java)
                        Log.d("ChoiceReviewAdapter", "selectedStar in adapter1: $selectedStar")
                        intent.putExtra("titleEng", clickedItem.TitleEng)
                        intent.putExtra("productId", clickedItem.productId)// Передача данных в следующую активность
                        intent.putExtra("selectedStars", selectedStarsList[position]) // Передача выбранного количества звезд
                        Log.d("ChoiceReviewAdapter", "selectedStar in adapter2: $selectedStar")

                        context.startActivity(intent)
                        Log.d("ChoiceReviewAdapter", "selectedStar in adapter3: $selectedStar")
                        notifyDataSetChanged()
                    }
                }
            }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceViewHolder {
        val binding = ItemChoiceReviewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        val choice = choiceList[position]
        holder.title.text = choice.Title
        holder.productId.text = choice.productId.toString()

        val selectedStars = selectedStarsList[position]

        holder.starImageViews.forEachIndexed { index, starImageView ->
            starImageView.setOnClickListener {
                selectedStarsList[position] = index + 1 // Установка выбранного количества звезд
                selectedStar = index + 1 // Установка выбранного количества звезд в переменную selectedStar
                notifyDataSetChanged() // Обновление адаптера
            }
            // Устанавливаем состояние звезд в зависимости от выбранного количества
            starImageView.setImageResource(if (index < selectedStars) R.drawable.ic_star_darkgreen else R.drawable.ic_star_dirtygreen)
        }

        val resourceId = context.resources.getIdentifier(choice.Image, "drawable", context.packageName)
        if(resourceId != 0){
            holder.productImage.setImageResource(resourceId)
        }
    }

    override fun getItemCount(): Int {
        return choiceList.size
    }
}