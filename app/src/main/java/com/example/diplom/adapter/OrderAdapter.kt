package com.example.diplom.adapter

import Order
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.CartEmptyFragment
import com.example.diplom.DatabaseHelper
import com.example.diplom.R
import com.example.diplom.databinding.ItemOrderLayoutBinding
import com.example.diplom.db.UserManager

class OrderAdapter(private val context: Context,
                   private val orderList: MutableList<Order>,
                   private val database: SQLiteDatabase,
                   private val fragmentManager: FragmentManager,
                   private val listener: OnOrderUpdateListener,
                   private val updateOrderList: () -> Unit // Добавляем поле для функции обновления списка заказов
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>(){

    private var onProductClickListener: OnProductClickListener? = null
    private val likedProducts: MutableList<String> = mutableListOf()

    inner class OrderViewHolder(private val binding: ItemOrderLayoutBinding) : RecyclerView.ViewHolder(binding.root){
            val orderImageView = binding.orderImageView
            val productIdTextView = binding.productIdTextView
            val nameProductTextView = binding.nameProductTextView
            val nameProductEngTextView = binding.nameProductEngTextView
            val volumeTextView = binding.volumeTextView
            val priceTextView = binding.priceTextView
            val likedButton = binding.likedButton
            val quantity = binding.quantityTextView
            val plusButton = binding.plusButton
            val minusButton = binding.minusButton
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.productIdTextView.text = order.ProductId.toString() //поле из таблицы Product.ID
        holder.nameProductTextView.text = order.Title // Product.Title
        holder.nameProductEngTextView.text = order.TitleEng // Product.TitleEng
        holder.volumeTextView.text = order.Volume // поля из таблицы Product.Volume + UnitMeasurements.Abbreviation
        //holder.priceTextView.text = order.Price // поле из таблицы Product.Price
        holder.quantity.text = "${order.Quantity} шт"

        // Удаляем символ "₽" из строки с ценой и преобразуем оставшуюся строку в число
        val priceString = order.Price.replace(" ₽", "")
        val totalPrice = priceString.toDouble() * order.Quantity
        // Устанавливаем общую цену товара
        holder.priceTextView.text = String.format("%.0f ₽", totalPrice)


        val resourceId =
            context.resources.getIdentifier(order.Image, "drawable", context.packageName)
        if (resourceId != 0) {
            holder.orderImageView.setImageResource(resourceId)
        }

        // Проверяем, является ли продукт лайкнутым
        val isLiked = likedProducts.contains(order.ProductId.toString())

        // Устанавливаем соответствующую иконку для drawableStart кнопки likedButton
        val iconDrawable = if (isLiked) {
            // Если продукт лайкнут, используем иконку для лайка
            R.drawable.ic_like_darkgreen_fill
        } else {
            // Если продукт не лайкнут, используем иконку для не лайка
            R.drawable.ic_like_darkgreen_nofill
        }

        // Устанавливаем новую иконку для drawableStart
        holder.likedButton.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, 0, 0, 0)
        // Обрабатываем нажатие на кнопку лайка
        holder.likedButton.setOnClickListener {
            if (isLiked) {
                // Если продукт уже лайкнут, удаляем его из списка лайков
                removeFromLikes(order.ProductId.toString())
                likedProducts.remove(order.ProductId.toString())
            } else {
                // Если продукт не лайкнут, добавляем его в список лайков
                addToLikes(order.ProductId.toString())
                likedProducts.add(order.ProductId.toString())
            }

            // Обновляем иконку кнопки лайка после изменения списка лайков
            val updatedIconDrawable = if (likedProducts.contains(order.ProductId.toString())) {
                R.drawable.ic_like_darkgreen_fill
            } else {
                R.drawable.ic_like_darkgreen_nofill
            }
            holder.likedButton.setCompoundDrawablesWithIntrinsicBounds(updatedIconDrawable, 0, 0, 0)
            // Уведомляем адаптер об изменениях в элементе списка
            notifyItemChanged(holder.adapterPosition)
        }

        // Обработчик нажатия на кнопку "plusButton"
        holder.plusButton.setOnClickListener {
            // Увеличиваем количество товаров в базе данных
            increaseQuantity(order.ProductId)
            // Обновляем количество товаров в списке и RecyclerView
            val updatedOrder = order.copy(Quantity = order.Quantity + 1)
            (orderList as MutableList<Order>)[position] = updatedOrder
            notifyItemChanged(position)

        }

        holder.minusButton.setOnClickListener {
            if (order.Quantity > 1) {
                decreaseQuantity(order.ProductId, position)
                val updatedOrder = order.copy(Quantity = order.Quantity - 1)
                (orderList as MutableList<Order>)[position] = updatedOrder
                notifyItemChanged(position)
            } else {
                // Если Quantity уже равно 1, вы можете выполнить другие действия или просто игнорировать нажатие кнопки минус
                deleteOrder(order.ProductId, position)
                notifyItemChanged(position)
            }
        }

        holder.itemView.setOnClickListener{
            val productId = orderList[position].ProductId
            onProductClickListener?.onProductClick(productId)
        }

        listener.onOrderUpdated()
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    private fun increaseQuantity(productId: Int) {
        val query = "UPDATE CartClient SET Quantity = Quantity + 1 WHERE Clients_id = ${UserManager.userId} AND Products_id = $productId"
        database.execSQL(query)
        notifyDataSetChanged()
    }

    private fun decreaseQuantity(productId: Int, position: Int) {
        val query = "UPDATE CartClient SET Quantity = Quantity - 1 WHERE Clients_id = ${UserManager.userId} AND Products_id = $productId"
        database.execSQL(query)

        // Проверяем, стало ли количество товара равным нулю после уменьшения
        val checkQuery = "SELECT Quantity FROM CartClient WHERE Clients_id = ${UserManager.userId} AND Products_id = $productId"
        val cursor = database.rawQuery(checkQuery, null)
        cursor.moveToFirst()
        val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))
        cursor.close()

        if (position != RecyclerView.NO_POSITION && quantity > 0) {
            // Обновляем данные в адаптере
            val updatedOrder = orderList[position].copy(Quantity = quantity)
            (orderList as MutableList<Order>)[position] = updatedOrder
            notifyItemChanged(position)
        } else if (position != RecyclerView.NO_POSITION && quantity == 0) {
            // Если количество равно нулю, удаляем запись из базы данных и уведомляем адаптер
            //deleteOrder(productId, position)
        }
    }

    private fun deleteOrder(productId: Int, position: Int) {
        val deleteQuery = "DELETE FROM CartClient WHERE Clients_id = ${UserManager.userId} AND Products_id = $productId"
        database.execSQL(deleteQuery)

        // Удаляем элемент из списка заказов
        (orderList as MutableList<Order>).removeAt(position)
        // Уведомляем адаптер об изменении данных
        notifyItemRemoved(position)

        if (orderList.isEmpty()) {
            val cartEmptyFragment = CartEmptyFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.frame_container, cartEmptyFragment)
                .commit()
        }
    }


    fun updateLikedProducts(likedProducts: List<String>) {
        this.likedProducts.clear()
        this.likedProducts.addAll(likedProducts)
        notifyDataSetChanged()
    }

    private fun removeFromLikes(productId: String) {
        val query = "DELETE FROM LikesClient WHERE Products_id = $productId AND Clients_id = ${UserManager.userId}"
        database.execSQL(query)
    }

    private fun addToLikes(productId: String) {
        val databaseHelper = DatabaseHelper(context)
        val database = databaseHelper.writableDatabase
        val clientId = UserManager.userId

        // Проверяем, существует ли уже запись в таблице LikesClient для данного клиента и продукта
        val query = "SELECT * FROM LikesClient WHERE Clients_id = ? AND Products_id = ?"
        val selectionArgs = arrayOf(clientId, productId)
        val cursor = database.rawQuery(query, selectionArgs)

        if (cursor != null && cursor.moveToFirst()) {
            // Если запись уже существует, можно выполнить операцию обновления, если это необходимо
            // Например, если вы хотите изменить какие-то данные в записи
            // В этом случае вы можете выполнить операцию update вместо insert
            // database.update("LikesClient", contentValues, "Clients_id = ? AND Products_id = ?", arrayOf(clientId, productId.toString()))

            // Здесь вы можете добавить код для выполнения операций обновления

            cursor.close()
        } else {
            // Если запись не существует, вставляем новую запись
            val contentValues = ContentValues().apply {
                put("Clients_id", clientId)
                put("Products_id", productId)
                // Здесь вы можете добавить другие поля, которые необходимо вставить
            }
            database.insert("LikesClient", null, contentValues)
        }
    }

    interface OnOrderUpdateListener {
        fun onOrderUpdated()
    }

    interface OnProductClickListener {
        fun onProductClick(productId: Int)
    }

    fun setOnProductClickListener(listener: OnProductClickListener) {
        this.onProductClickListener = listener
    }

    fun updateOrderList(newOrderList: List<Order>) {
        orderList.clear()
        orderList.addAll(newOrderList)
        notifyDataSetChanged()
    }

}
