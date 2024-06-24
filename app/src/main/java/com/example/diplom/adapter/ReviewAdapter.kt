package com.example.diplom.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.databinding.ItemReviewLayoutBinding

class ReviewAdapter(private val context: Context, private val reviewList: List<Review>) :RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {


    inner class ReviewViewHolder(private val binding: ItemReviewLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val clientNameTextView = binding.clientNameTextView
        val starImageView = binding.starImageView
        //val dateTextView = binding.dateTextView
        val plusTextView = binding.plusTextView
        val minusTextView = binding.minusTextView
        val commentTextView = binding.commentTextView
        val titlePlusTextView = binding.titlePlusTextView
        val titleMinusTextView = binding.titleMinusTextView
        val titleCommentTextView = binding.titleCommentTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewAdapter.ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        Log.d(TAG, "onBindViewHolder() вызван для позиции $position: Int")
        holder.clientNameTextView.text = review.clientFirstName

        val scoreId = context.resources.getIdentifier(review.score.toString(), "drawable", context.packageName)
        if(scoreId != 0){
            holder.starImageView.setImageResource(scoreId)
        }

        // Set text and visibility for plusTextView
        if (review.plus.isNullOrEmpty()) {
            holder.plusTextView.visibility = View.GONE
            holder.titlePlusTextView.visibility = View.GONE
        } else {
            holder.plusTextView.visibility = View.VISIBLE
            holder.plusTextView.text = review.plus
        }

        // Set text and visibility for minusTextView
        if (review.minus.isNullOrEmpty()) {
            holder.minusTextView.visibility = View.GONE
            holder.titleMinusTextView.visibility = View.GONE
        } else {
            holder.minusTextView.visibility = View.VISIBLE
            holder.minusTextView.text = review.minus
        }

        // Set text and visibility for commentTextView
        if (review.comment.isNullOrEmpty()) {
            holder.commentTextView.visibility = View.GONE
            holder.titleCommentTextView.visibility = View.GONE
        } else {
            holder.commentTextView.visibility = View.VISIBLE
            holder.commentTextView.text = review.comment
        }

        //holder.dateTextView.text = review.date
    }


    override fun getItemCount(): Int {
        return reviewList.size
    }
}
