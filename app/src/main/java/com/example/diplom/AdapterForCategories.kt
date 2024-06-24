package com.example.diplom

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class AdapterForCategories(private val listArray: ArrayList<categoriesItem>, private val context: Context) : RecyclerView.Adapter<AdapterForCategories.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.item_categories_layout, parent, false))
    }

    override fun getItemCount(): Int = listArray.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listItem = listArray[position]
        holder.bind(listItem)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle = view.findViewById<TextView>(R.id.nameCategories)
        private val tvImage = view.findViewById<ImageView>(R.id.icCategories)

        init {
            itemView.setOnClickListener {
                        val categoryId = listArray[adapterPosition].categoryId
                        val fragment = CategoriesFragment.newInstance(categoryId)
                        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_container, fragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
                }
            }
            fun bind(listItem: categoriesItem) {
                tvTitle.text = listItem.titleText
                tvImage.setImageResource(listItem.image_id)
            }
        }

        private fun openFragment(fragment: Fragment) {
            val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
}

