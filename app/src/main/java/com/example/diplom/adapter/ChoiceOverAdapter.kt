package com.example.diplom.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.R
import org.w3c.dom.Text

class ChoiceOverAdapter(val context: Context, val list2: ArrayList<ChoiceOver>): RecyclerView.Adapter<ChoiceOverAdapter.Link>() {

    private var choiceOverTextView: TextView? = null
    private var selectedChoiceOverFrameLayout: FrameLayout? = null
    private var selectedChoiceOverTextView: TextView? = null
    private var choiceOverFrameLayout: FrameLayout? = null

    class Link(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image_id: ImageView = itemView.findViewById(R.id.overImageView)
        val title_id: TextView = itemView.findViewById(R.id.titleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceOverAdapter.Link {
        val root = LayoutInflater.from(context).inflate(R.layout.item_choice_over_layout, parent, false)
        return Link(root)
    }

    override fun onBindViewHolder(holder: ChoiceOverAdapter.Link, position: Int) {
        val choiceOver = list2[position]

        holder.image_id.setImageResource(list2[position].image_Id)
        holder.title_id.setText(list2[position].title)

        holder.itemView.setOnClickListener{
            selectedChoiceOverTextView?.text = choiceOver.title
            choiceOverTextView?.visibility = View.GONE
            selectedChoiceOverFrameLayout?.visibility = View.VISIBLE
            choiceOverFrameLayout?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list2.size
    }

    //поменять текст
    fun setSelectedChoiceOverTextView(textView: TextView){
        selectedChoiceOverTextView = textView
    }

    //убрать текстВью
    fun setChoiceOverTextView(textView: TextView){
        choiceOverTextView = textView
    }

    //показать фрейм с выбраным
    fun setSelectedChoiceOverFrameLayout(frameLayout: FrameLayout){
        selectedChoiceOverFrameLayout = frameLayout
    }

    //убрать фрейм с выбором
    fun setChoiceOverFrameLayout(frameLayout: FrameLayout){
        choiceOverFrameLayout = frameLayout
    }
}