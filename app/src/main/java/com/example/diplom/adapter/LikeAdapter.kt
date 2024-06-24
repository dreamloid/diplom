package com.example.diplom.adapter

import ProductAdapter
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.CartEmptyFragment
import com.example.diplom.DatabaseHelper
import com.example.diplom.LikesEmptyFragment
import com.example.diplom.R
import com.example.diplom.databinding.ItemLikeLayoutBinding
import com.example.diplom.db.UserManager

class LikeAdapter(private val context: Context, private var likeList: MutableList<Like>, private val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<LikeAdapter.LikeViewHolder>() {

    private var onProductClickListener: OnProductClickListener? = null
    private var onAddToCartClickListener: OnAddToCartClickListener? = null

    private var onAddProductClickListener: OnAddProductClickListener? = null
    private var onDeleteProductClickListener: OnDeleteProductClickListener? = null

    inner class LikeViewHolder(private val binding: ItemLikeLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleTextView = binding.titleTextView
        val titleEngTextView = binding.titleEngTextView
        val volumeTextView = binding.volumeTextView
        val priceTextView = binding.priceTextView
        val productImageView = binding.productImageView

        val firstProductFrameLayout = binding.firstProductFrameLayout
        val addMoreProductFrameLayout = binding.addMoreProductFrameLayout
        val quantityTextView = binding.quantityTextView

        val buttonAddCart = binding.addCartButton
        val buttonAddProduct = binding.addMoreCartButton
        val buttonDeleteProduct = binding.deleteCartButton
        val buttonLikeProduct = binding.likeButton

        init {
            buttonAddCart.setOnClickListener{
                val productId = likeList[adapterPosition].productId
                onAddToCartClickListener?.onAddToCartClick(productId)
            }

            buttonAddProduct.setOnClickListener{
                val productId = likeList[adapterPosition].productId
                onAddProductClickListener?.onAddProductClick(productId)
            }

            buttonDeleteProduct.setOnClickListener{
                val productId = likeList[adapterPosition].productId
                onDeleteProductClickListener?.onDeleteProductClick(productId)
            }

            buttonLikeProduct.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    deleteLikeProduct(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val binding = ItemLikeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val like = likeList[position]
        holder.titleTextView.text = like.Title
        holder.titleEngTextView.text = like.TitleEng
        holder.volumeTextView.text = like.Volume
        holder.priceTextView.text = like.Price

        // Получаем количество товара в корзине
        val productId = like.productId
        val quantity = getQuantityInCart(productId)

        // Устанавливаем количество в quantityTextView
        holder.quantityTextView.text = quantity

        val resourceId = context.resources.getIdentifier(like.ImageName, "drawable", context.packageName)
        if(resourceId != 0) {
            holder.productImageView.setImageResource(resourceId)
        }

        val isInCart = checkIfProductInCart(like.productId)

        if (isInCart) {
            holder.firstProductFrameLayout.visibility = View.GONE
            holder.addMoreProductFrameLayout.visibility = View.VISIBLE
        } else {
            holder.firstProductFrameLayout.visibility = View.VISIBLE
            holder.addMoreProductFrameLayout.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{
            val productId = likeList[position].productId
            onProductClickListener?.onProductClick(productId)
        }
    }

    private fun deleteLikeProduct(position: Int) {
        val likeToDelete = likeList[position]

        Log.e("Like", "Выбранный товар: $likeToDelete")

        try {
            val dbHelper = DatabaseHelper(context)
            val database = dbHelper.writableDatabase

            Log.d("Like", "ID товара для удаления: ${likeToDelete.productId}")

            // Удаляем запись из таблицы LikeClients
            val whereClauseClientsLikeProduct = "Clients_id = ? and Products_id = ?"
            val whereArgsClientsLikeProduct = arrayOf(UserManager.userId.toString(), likeToDelete.productId.toString())
            val deletedRows = database.delete("LikesClient", whereClauseClientsLikeProduct, whereArgsClientsLikeProduct)

            // Проверяем, была ли успешно удалена запись из базы данных
            if (deletedRows > 0) {
                // Если удаление из базы данных успешно, удаляем элемент из списка и уведомляем адаптер
                likeList.removeAt(position)
                notifyItemRemoved(position)

                //Проверяем, если список пуст, перенаправляем пользователя на другой экран
                if (likeList.isEmpty()) {
                    // Например, переход на другой фрагмент или активити
                    val likeEmptyFragment = LikesEmptyFragment()
                    fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, likeEmptyFragment)
                        .commit()
                }

            } else {
                // Если удаление из базы данных не удалось, выводим сообщение об ошибке
                Log.e("Like", "Ошибка при удалении записи из базы данных")
                Toast.makeText(context, "Ошибка при удалении записи из базы данных", Toast.LENGTH_SHORT).show()
            }

            database.close()
        } catch (e: Exception) {
            // Обрабатываем ошибку при удалении из базы данных
            Log.e("Like", "Ошибка при удалении ${e.message}")
            Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show()
        }
    }


    override fun getItemCount(): Int {
        return likeList.size
    }

    // Метод для проверки, добавлен ли продукт в корзину
    fun checkIfProductInCart(productId: Int): Boolean {
        // Получаем доступ к базе данных и выполняем запрос для проверки, есть ли продукт в корзине
        val databaseHelper = DatabaseHelper(context)
        val database = databaseHelper.readableDatabase

        val clientId = UserManager.userId
        val query = "SELECT COUNT(*) FROM CartClient WHERE Clients_id = $clientId AND Products_id = $productId"
        val cursor = database.rawQuery(query, null)

        var isInCart = false
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val count = cursor.getInt(0)
                isInCart = count > 0
            }
            cursor.close()
        }

        return isInCart
    }

    // Функция для получения количества товара в корзине
    private fun getQuantityInCart(productId: Int): String {
        val databaseHelper = DatabaseHelper(context)
        val database = databaseHelper.readableDatabase

        val clientId = UserManager.userId

        // Формируем запрос к таблице CartClient для получения количества товара в корзине для заданного productId
        val query = "SELECT Quantity FROM CartClient WHERE Clients_id = $clientId AND Products_id = $productId"
        val cursor = database.rawQuery(query, null)

        var quantityString = "0 шт"
        if (cursor != null && cursor.moveToFirst()) {
            val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))
            quantityString = "$quantity шт"
            cursor.close()
        }

        return quantityString
    }

    interface OnProductClickListener {
        fun onProductClick(productId: Int)
    }

    interface OnAddToCartClickListener {
        fun onAddToCartClick(productId: Int)
    }

    interface OnAddProductClickListener {
        fun onAddProductClick(productId: Int)
    }

    interface OnDeleteProductClickListener {
        fun onDeleteProductClick(productId: Int)
    }

    //переход на продукт
    fun setOnProductClickListener(listener: OnProductClickListener) {
        this.onProductClickListener = listener
    }

    //добавить продукт первый раз
    fun setOnAddToCartClickListener(listener: OnAddToCartClickListener) {
        this.onAddToCartClickListener = listener
    }

    //добавить еще
    fun setOnAddProductClickListener(listener: OnAddProductClickListener) {
        this.onAddProductClickListener = listener
    }

    //удалить продукт
    fun setOnDeleteProductClickListener(listener: OnDeleteProductClickListener) {
        this.onDeleteProductClickListener = listener
    }
}