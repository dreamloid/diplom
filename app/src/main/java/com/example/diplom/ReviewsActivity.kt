package com.example.diplom

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplom.adapter.Review
import com.example.diplom.adapter.ReviewAdapter
import com.example.diplom.databinding.ActivityReviewsBinding
import com.example.diplom.databinding.ItemReviewLayoutBinding
import com.example.diplom.db.ProductManager
import com.example.diplom.db.UserManager
import com.example.diplom.decorator.HorizontalSpaceItemDecoration
import com.example.diplom.decorator.VerticalSpaceItemDecoration
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewsBinding
    private lateinit var reviewAdapter: ReviewAdapter
    private var reviewList: List<Review> = emptyList()
    private var productId: Int ?= null
    private lateinit var addCardButton: Button
    private lateinit var deleteCardButton: Button
    private lateinit var quantityTextView: TextView
    private lateinit var db: SQLiteDatabase
    private lateinit var productImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "onCreate() вызван")

        val binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.binding = binding
        // Получаем доступ к textView
        val countTextView: TextView = findViewById(R.id.textViewCountReviews)
        val titleTextView: TextView = findViewById(R.id.textViewProduct)
        val volumeTextView: TextView = findViewById(R.id.textViewVolume)
        val priceTextView: TextView = findViewById(R.id.textViewPrice)
        addCardButton = findViewById(R.id.addCardButton)
        deleteCardButton = findViewById(R.id.deleteCardButton)
        quantityTextView = findViewById(R.id.quantityTextView)
        productImageView = findViewById(R.id.imageViewProduct)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val databaseHelper = DatabaseHelper(this)
        db = databaseHelper.readableDatabase
        val database = databaseHelper.readableDatabase
        this.reviewList = getReviewsFromDatabase(database)

        val userId = UserManager.userId!! // Получаем id пользователя
        val productId = ProductManager.productId!! // Получаем id продукта

        // Проверяем, существует ли запись для данного пользователя и продукта
        checkCartItem(userId, productId)

        val verticalSpaceItemDecoration = VerticalSpaceItemDecoration(this, 32) //отступ для последнего элеметна

        reviewAdapter = ReviewAdapter(this, reviewList)
        binding.reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reviewRecyclerView.adapter = reviewAdapter
        binding.reviewRecyclerView.addItemDecoration(verticalSpaceItemDecoration) // применение отступа для последнего элемента
        reviewAdapter.notifyDataSetChanged()

        addCardButton.setOnClickListener {

            // Проверяем, существует ли запись для данного пользователя и продукта
            val quantity = checkCartItem(userId, productId)

            if (quantity > 0) {
                // Запись уже существует, увеличиваем количество
                updateCartItem(userId, productId, quantity + 1)
            } else {
                // Запись не существует, добавляем новую
                addNewItemToCart(userId, productId)
            }
        }

        deleteCardButton.setOnClickListener {
            decreaseQuantity(userId, productId)
        }

        // Подсчитываем количество записей
        val count = getCountOfReviews(database)
        // Получаем правильное склонение слова "отзыв"
        val suffix = getReviewCountSuffix(count)
        // Устанавливаем значение в textView
        countTextView.text = "$count отзыв$suffix"

        getProductFromDatabase(database, titleTextView, volumeTextView, priceTextView, productImageView)
    }


    private fun checkCartItem(userId: String, productId: Int): Int {
        val query = "SELECT Quantity FROM CartClient WHERE Clients_id = $userId AND Products_id = $productId"
        val cursor = db.rawQuery(query, null)
        var quantity = 0

        if (cursor.moveToFirst()) {
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))
            // Показываем элементы удаления товара, если запись существует
            deleteCardButton.visibility = View.VISIBLE
            quantityTextView.visibility = View.VISIBLE
            // Заполняем TextView количеством
            quantityTextView.text = "${quantity} шт"
        } else {
            // Скрываем элементы удаления товара, если запись не существует
            deleteCardButton.visibility = View.GONE
            quantityTextView.visibility = View.GONE
        }

        cursor.close()
        return quantity
    }

    private fun addNewItemToCart(userId: String, productId: Int) {
        val values = ContentValues().apply {
            put("Clients_id", userId)
            put("Products_id", productId)
            put("Quantity", 1)
        }

        db.insert("CartClient", null, values)
        // Показываем элементы удаления товара
        deleteCardButton.visibility = View.VISIBLE
        quantityTextView.visibility = View.VISIBLE
        // Устанавливаем количество в 1
        quantityTextView.text = "1 шт"
    }

    private fun updateCartItem(userId: String, productId: Int, newQuantity: Int) {
        val values = ContentValues().apply {
            put("Quantity", newQuantity)
        }

        db.update("CartClient", values, "Clients_id = $userId AND Products_id = $productId", null)
        // Обновляем отображаемое количество
        quantityTextView.text = "${newQuantity} шт"
    }

    // Функция для уменьшения количества товара
    private fun decreaseQuantity(userId: String, productId: Int) {
        val query = "SELECT Quantity FROM CartClient WHERE Clients_id = $userId AND Products_id = $productId"
        val cursor = db.rawQuery(query, null)
        var quantity = 0

        if (cursor.moveToFirst()) {
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))

            if (quantity > 1) {
                updateCartItem(userId, productId, quantity - 1)
            } else {
                // Если количество достигло 0, удаляем запись
                db.delete("CartClient", "Clients_id = $userId AND Products_id = $productId", null)
                // Скрываем элементы удаления товара
                deleteCardButton.visibility = View.GONE
                quantityTextView.visibility = View.GONE
            }
        }

        cursor.close()
    }

    private fun getProductFromDatabase(database: SQLiteDatabase,
                                       titleTextView: TextView,
                                       volumeTextView: TextView,
                                       priceTextView: TextView,
                                       imageProduct: ImageView){
        productId = ProductManager.productId

        val query = "select Title, Volume, Price, TitleEng, Abbreviation from Products, UnitMeasurements where Products.UnitMeasurements_id = UnitMeasurements.ID and Products.ID = $productId"
        val cursor = database.rawQuery(query, null)

        if(cursor.moveToFirst()){
            val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))

            val volume = cursor.getDouble(cursor.getColumnIndexOrThrow("Volume"))
            val formattedVolume = String.format("%.0f", volume)

            val unitMeasurements = cursor.getString(cursor.getColumnIndexOrThrow("Abbreviation"))

            val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

            val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
            val formattedPrice = String.format("%.0f", price)

            val totalPrice = "$formattedPrice ₽"
            val totalVolume = "$formattedVolume $unitMeasurements"

            val imageName = "png_$titleEng"

            titleTextView.text = title
            volumeTextView.text = totalVolume
            priceTextView.text = totalPrice

            loadImage(this, imageName, imageProduct)
        }
        cursor.close()
    }

    // Функция для загрузки изображения по его имени
    private fun loadImage(context: Context, imageName: String, imageView: ImageView) {
        val imageResource = context.resources.getIdentifier(imageName, "drawable", context.packageName)

        if (imageResource != 0) {
            // Найдено соответствующее изображение
            imageView.setImageResource(imageResource)
        } else {
            // Изображение не найдено, установите изображение заглушку или другое по умолчанию
            imageView.setImageResource(R.drawable.ic_like)
        }
    }

    private fun getReviewsFromDatabase(database: SQLiteDatabase): List<Review> {
        val reviewList = mutableListOf<Review>()
        try{
            val query = "select Reviews.Score, Reviews.Dignities, Reviews.Disadvantages, Reviews.Comment, Clients.FirstName " +
                        "from Reviews " +
                        "join Clients on Reviews.Client_id = Clients.ID  " +
                        "join Products on Reviews.Products_id = Products.ID  " +
                        "where Products.ID = ${ProductManager.productId} "
            val cursor = database.rawQuery(query, null)
            if(cursor.moveToFirst()){
                do{
                    val score = cursor.getInt(cursor.getColumnIndexOrThrow("Score"))
                    val scoreImage = "ic_star_count_$score"
                    val dignities = cursor.getString(cursor.getColumnIndexOrThrow("Dignities"))

                    val disadvantages = cursor.getString(cursor.getColumnIndexOrThrow("Disadvantages"))

                    val comment = cursor.getString(cursor.getColumnIndexOrThrow("Comment"))

                    val firstName = cursor.getString(cursor.getColumnIndexOrThrow("FirstName"))

                    //val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("Date"))

                    //val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    //val date = dateFormat.parse(dateStr)

                    // Форматируем дату и время в требуемый формат "31 янв."
                    //val formattedDate = SimpleDateFormat("dd MMM", Locale("ru")).format(date)

                    val reivew = Review(firstName, scoreImage, dignities, disadvantages, comment) //, formattedDate)
                    reviewList.add(reivew)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка в получении отзывов из БД: ${e.message}")
        }
        return reviewList
    }

    private fun getCountOfReviews(database: SQLiteDatabase): Int {
        var count = 0
        try {
            val query = "SELECT COUNT(*) FROM Reviews where Products_id =${ProductManager.productId}"
            val cursor = database.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при подсчете количества записей: ${e.message}")
        }
        return count
    }

    private fun getReviewCountSuffix(count: Int): String {
        return if (count % 100 in 11..19) {
            "ов"
        } else {
            when (count % 10) {
                1 -> ""
                in 2..4 -> "а"
                else -> "ов"
            }
        }
    }

    fun onBackButtonClick(view: View) {
        onBackPressed()
    }
}