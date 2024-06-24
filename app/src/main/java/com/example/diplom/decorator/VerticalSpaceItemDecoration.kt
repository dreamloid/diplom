package com.example.diplom.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration (
    private val context: Context,
    private val verticalSpace: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        if (position == itemCount - 1) {
            // Добавляем отступ только для последнего элемента
            outRect.bottom = verticalSpace
        } else {
            // Для остальных элементов не добавляем отступ справа
            outRect.bottom = 0
        }
    }
}