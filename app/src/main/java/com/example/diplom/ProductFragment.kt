package com.example.diplom

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplom.adapter.Similar
import com.example.diplom.adapter.SimilarAdapter
import com.example.diplom.databinding.FragmentProductBinding
import com.example.diplom.db.CategoryManager
import com.example.diplom.db.ProductManager
import com.example.diplom.db.UserManager
import com.example.diplom.decorator.HorizontalSpaceItemDecoration
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentProductBinding
    private lateinit var similarAdapter: SimilarAdapter
    private var similarList: List<Similar> = emptyList()

    private lateinit var avgRatingTextView: TextView
    private lateinit var numberFeedbackTextView: TextView
    private lateinit var countFeedbackTextView: TextView
    private lateinit var likeButton: Button
    private lateinit var addMoreCartButton: Button
    private lateinit var deleteCartButton: Button

    private var productId: Int ?= null
    private var categoryId: Int ?= null
    private var previousFragmentId: Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductBinding.inflate(inflater, container, false)
        val view = binding.root

        avgRatingTextView = view.findViewById(R.id.avgRatin)
        numberFeedbackTextView = view.findViewById(R.id.numberFeedbackTextView)
        countFeedbackTextView = view.findViewById(R.id.numberFeedback2TextView)
        likeButton = view.findViewById(R.id.likeButton)
        addMoreCartButton = view.findViewById(R.id.addMoreCartButton)
        deleteCartButton = view.findViewById(R.id.deleteCartButton)

        val addToCartFrameLayout = view.findViewById<FrameLayout>(R.id.addToCartFrameLayout)
        val addToCartMoreFrameLayout = view.findViewById<FrameLayout>(R.id.addToCartMoreFrameLayout)
        val totalPriceTextView = view.findViewById<TextView>(R.id.totalPriceTextView)
        val quantityTextView = view.findViewById<TextView>(R.id.quantityTextView)

        val productId: Int = ProductManager.productId ?: 0 // если ProductManager.productId может быть null, используем значение по умолчанию (например, 0)
        val userId: String = UserManager.userId ?: "0" // если UserManager.userId может быть null, используем значение по умолчанию (например, 0)

        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase
        val horizontalSpaceItemDecoration = HorizontalSpaceItemDecoration(requireContext(), 24) //отступ для последнего элеметна
        this.similarList = getSimilarFromDatabase(database)

        val likeButtonHandler = LikeButtonHandler(databaseHelper, likeButton)

        likeButton.setOnClickListener {
            val productId = ProductManager.productId ?: return@setOnClickListener // Проверка на null
            val userId = UserManager.userId ?: return@setOnClickListener // Проверка на null
            likeButtonHandler.handleLikeButtonClick(productId, userId)
        }

        val cartButtonHandler = CartButtonHandler(databaseHelper, addToCartFrameLayout, addToCartMoreFrameLayout, quantityTextView, totalPriceTextView)

        addMoreCartButton.setOnClickListener {
            cartButtonHandler.handleAddMoreClick(productId, userId)
        }

        deleteCartButton.setOnClickListener {
            cartButtonHandler.handleDeleteClick(productId, userId)
        }

        addToCartFrameLayout.setOnClickListener{
            cartButtonHandler.handleAddToCartClick(productId, userId)
        }

        similarAdapter = SimilarAdapter(requireContext(), similarList)
        binding.similarRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.similarRecyclerView.adapter = similarAdapter
        binding.similarRecyclerView.addItemDecoration(horizontalSpaceItemDecoration) // применение отступа для последнего элемента



        // Вызовите методы для отображения информации об отзывах и оценках
        displayAvgRating(database)
        displayNumberOfFeedbacks(database)
        displayCountOfFeedbacks(database)
        checkProductInDatabase(database, productId, userId, likeButton, requireContext())
        checkProductInCart(database, productId, userId, addToCartFrameLayout, addToCartMoreFrameLayout, quantityTextView, totalPriceTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = view.findViewById<TextView>(R.id.textViewIdProduct)
        productId.text = ProductManager.productId.toString()

        val categoryIdTextView = view.findViewById<TextView>(R.id.textViewIdCategory)
        categoryIdTextView.text = CategoryManager.categoryId.toString()

        val arrowButton = view.findViewById<Button>(R.id.ArrowButton)

        arrowButton.setOnClickListener{
            Log.d("ProfileFragment", "arrowButton clicked")
            // Проверяем, что categoryId не null перед использованием
            if(previousFragmentId == 1){
                categoryId?.let { categoryIdValue ->
                    val args = Bundle().apply {
                        putInt("categoryId", categoryIdValue)
                    }
                    val categoryFragment = CategoriesFragment().apply {
                        arguments = args
                    }
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_container, categoryFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            } else {
                requireActivity().onBackPressed()
            }

        }

        val titleTextView = view.findViewById<TextView>(R.id.titleEngTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val volumeTextView = view.findViewById<TextView>(R.id.volumeTextView)
        val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
        val productImageView = view.findViewById<ImageView>(R.id.productImageView)
        val plusTextView = view.findViewById<TextView>(R.id.plusTextView)
        val minusTextView = view.findViewById<TextView>(R.id.minusTextView)
        val commentTextView = view.findViewById<TextView>(R.id.commentEditText)
        val clientNameTextView = view.findViewById<TextView>(R.id.clientNameTextView)
        val starImageView = view.findViewById<ImageView>(R.id.starImageView)
        val reviewFrameLayout = view.findViewById<ConstraintLayout>(R.id.reviewFrameLayout)
        val price2TextView = view.findViewById<TextView>(R.id.price2TextView)
        //val dateTextView = view.findViewById<TextView>(R.id.dateTextView)


        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase
        getInfoProduct(database, titleTextView, descriptionTextView, volumeTextView, priceTextView, price2TextView, productImageView)
        getInfoReviews(database, starImageView, plusTextView, minusTextView, commentTextView, clientNameTextView, reviewFrameLayout) // dateTextView, reviewFrameLayout)

        val reviewButton = view.findViewById<Button>(R.id.reviewButton)
        reviewFrameLayout.setOnClickListener{
            val intent = Intent(activity, ReviewsActivity::class.java)
            startActivity(intent)
        }

        reviewButton.setOnClickListener {
            val intent = Intent(activity, ReviewsActivity::class.java)
            startActivity(intent)
        }

        similarAdapter.setOnProductClickListener(object : SimilarAdapter.OnProductClickListener{
            override fun onProductClick(productId: Int, categoryId: Int) {
                ProductManager.productId = productId
                CategoryManager.categoryId = categoryId
                val fragment = ProductFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        })

    }

    //Получить тнформацию о отзывах
    fun getInfoReviews(database: SQLiteDatabase,
                       scoreImageView: ImageView,
                       plusTextView: TextView,
                       minusTextView: TextView,
                       commentTextView: TextView,
                       clientTextView: TextView,
                       //dateTextView: TextView,
                       reviewFrameLayout: ConstraintLayout){

        val query = "select Reviews.Score, Reviews.Dignities, Reviews.Disadvantages, Reviews.Comment, Clients.FirstName " +
                    "from Reviews " +
                    "join Clients on Reviews.Client_id = Clients.ID  " +
                    "join Products on Reviews.Products_id = Products.ID  " +
                    "where Products.ID = ${ProductManager.productId} " +
                    "order by random() " +
                    "limit 1"
        val cursor = database.rawQuery(query, null)
        if(cursor.moveToFirst()){
            val score = cursor.getInt(cursor.getColumnIndexOrThrow("Score"))
            val scoreImage = "ic_star_count$score"

            val dignities = cursor.getString(cursor.getColumnIndexOrThrow("Dignities"))
            val disadvantages = cursor.getString(cursor.getColumnIndexOrThrow("Disadvantages"))
            val comment = cursor.getString(cursor.getColumnIndexOrThrow("Comment"))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow("FirstName"))

            // Устанавливаем изображение в ImageView
            val context = scoreImageView.context
            val imageResource = context.resources.getIdentifier(scoreImage, "drawable", context.packageName)
            if (imageResource != 0) {
                scoreImageView.setImageResource(imageResource)
            }

            //val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("Date"))

            //val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            //val date = dateFormat.parse(dateStr)

            // Форматируем дату и время в требуемый формат "31 янв."
            //val formattedDate = SimpleDateFormat("dd MMM", Locale("ru")).format(date)

            plusTextView.text = dignities
            minusTextView.text = disadvantages
            commentTextView.text = comment
            clientTextView.text = firstName

            //dateTextView.text = "formattedDate"

            reviewFrameLayout.visibility = View.VISIBLE
        } else{
            reviewFrameLayout.visibility = View.GONE
        }
        cursor.close()
    }

    // получить информацию о выбранном продукте
    fun getInfoProduct(database: SQLiteDatabase,
                       titleTextView: TextView,
                       descriptionTextView: TextView,
                       volumeTextView: TextView,
                       priceTextView: TextView,
                       price2TextView: TextView,
                       productImageView: ImageView){
        productId = ProductManager.productId
        categoryId = CategoryManager.categoryId

        val query = "select Title, Description, Volume, UnitMeasurements.Abbreviation, Price, TitleEng from Products, UnitMeasurements where UnitMeasurements.ID = Products.UnitMeasurements_id and Products.ID = $productId"
        val cursor = database.rawQuery(query, null)

        // Проверяем, что результирующий набор не пустой
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))

            val description = cursor.getString(cursor.getColumnIndexOrThrow("Description"))

            val volume = cursor.getDouble(cursor.getColumnIndexOrThrow("Volume"))
            val formattedVolume = String.format("%.0f", volume)

            val unitMeasurements = cursor.getString(cursor.getColumnIndexOrThrow("Abbreviation"))

            val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
            val formattedPrice = String.format("%.0f", price)

            val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

            // Имя ресурса изображения (заглушка)
            val imageName = "png_big_$titleEng" // Предполагаем, что у вас есть ресурсы с именами, соответствующими наименованию товара

            val totalPrice = "$formattedPrice ₽"
            val totalVolume = "$formattedVolume $unitMeasurements"

            titleTextView.text = title
            descriptionTextView.text = description
            volumeTextView.text = totalVolume
            priceTextView.text = totalPrice
            price2TextView.text = totalPrice
            loadImage(imageName, productImageView) // Загружаем изображение
        }

        cursor.close()
    }

    // Функция для загрузки изображения по его имени
    private fun loadImage(imageName: String, imageView: ImageView) {
        val context = requireContext()
        val imageResource = context.resources.getIdentifier(imageName, "drawable", context.packageName)

        if (imageResource != 0) {
            // Найдено соответствующее изображение
            imageView.setImageResource(imageResource)
        } else {
            // Изображение не найдено, установите изображение заглушку или другое по умолчанию
            imageView.setImageResource(R.drawable.ic_like)
        }
    }

    // Функция для получения списка товаров из базы данных
    fun getSimilarFromDatabase(database: SQLiteDatabase): List<Similar> {
        val similarList = mutableListOf<Similar>()

        productId = ProductManager.productId
        categoryId = CategoryManager.categoryId

        val query = "select Products.ID as ProductID, ProductTypes.ID as ProductTypeID, Products.Title, Volume, Price, TitleEng, Abbreviation " +
                    "from Products, UnitMeasurements, ProductTypes " +
                    "where UnitMeasurements.ID = Products.UnitMeasurements_id " +
                    "and ProductTypes.ID = Products.ProductTypes_id " +
                    "and Products.ID not like $productId and ProductTypes_id = $categoryId"
        val cursor = database.rawQuery(query, null)

        // Проверяем, что результирующий набор не пустой
        if (cursor.moveToFirst()) {
            do {
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ProductID"))

                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("ProductTypeID"))

                val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))

                val volume = cursor.getDouble(cursor.getColumnIndexOrThrow("Volume"))
                val formattedVolume = String.format("%.0f", volume)

                val unitMeasurements = cursor.getString(cursor.getColumnIndexOrThrow("Abbreviation"))

                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
                val formattedPrice = String.format("%.0f", price)

                val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

                // Имя ресурса изображения (заглушка)
                val imageName = "png_$titleEng" // Предполагаем, что у вас есть ресурсы с именами, соответствующими наименованию товара
                val totalPrice = "$formattedPrice ₽"
                val totalVolume = "$formattedVolume $unitMeasurements"
                val similar = Similar(productId, categoryId,  title, totalVolume, totalPrice, imageName, titleEng)
                similarList.add(similar)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return similarList
    }

    //Авг рейтинг товара
    private fun displayAvgRating(database: SQLiteDatabase) {
        // Выполните запрос SQL для получения среднего значения оценок
        val query = "SELECT AVG(Score) FROM Reviews where Reviews.Products_id = ${ProductManager.productId}"
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val avgRating = cursor.getFloat(0)
            // Округляем до десятых
            val roundedAvgRating = String.format("%.1f", avgRating)
            avgRatingTextView.text = "$roundedAvgRating"
        }
        cursor.close()
    }

    private fun displayNumberOfFeedbacks(database: SQLiteDatabase) {
        // Выполните запрос SQL для подсчета количества отзывов
        val query = "SELECT COUNT(*) FROM Reviews where Reviews.Products_id = ${ProductManager.productId}"
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val numberOfFeedbacks = cursor.getInt(0)
            // Используйте функцию для правильного склонения слова "отзыв"
            val suffix = getReviewCountSuffix(numberOfFeedbacks)
            numberFeedbackTextView.text = "$numberOfFeedbacks отзыв$suffix"
        }
        cursor.close()
    }

    private fun displayCountOfFeedbacks(database: SQLiteDatabase) {
        // Выполните запрос SQL для подсчета количества оценок
        val query = "SELECT COUNT(Score) FROM Reviews where Reviews.Products_id = ${ProductManager.productId}"
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val countOfFeedbacks = cursor.getInt(0)
            // Используйте функцию для правильного склонения слова "оценка"
            val suffix = getRatingCountSuffix(countOfFeedbacks)
            countFeedbackTextView.text = "$countOfFeedbacks оцен$suffix"
        }
        cursor.close()
    }

    // Функция для определения правильного склонения слова "отзыв"
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

    // Функция для определения правильного склонения слова "оценка"
    private fun getRatingCountSuffix(count: Int): String {
        return if (count % 100 in 11..19) {
            ""
        } else {
            when (count % 10) {
                1 -> "ка"
                in 2..4 -> "ки"
                else -> "ок"
            }
        }
    }

    // Метод для проверки наличия товара в базе данных у клиента
    fun checkProductInDatabase(database: SQLiteDatabase, productId: Int, userId: String, likeButton: Button, context: Context) {

        // Запрос к базе данных для проверки наличия товара у клиента
        val query = "SELECT * FROM LikesClient WHERE Clients_id = ? AND Products_id = ?"
        val selectionArgs = arrayOf(userId, productId.toString())
        val cursor = database.rawQuery(query, selectionArgs)

        // Проверка наличия результатов
        val productExistsInDatabase: Boolean = cursor.count > 0
        cursor.close()
        //database.close()

        // Получение ресурса drawable в зависимости от наличия товара в базе данных
        val drawable = if (productExistsInDatabase) {
            context.resources.getDrawable(R.drawable.ic_like_darkgreen_fill)
        } else {
            context.resources.getDrawable(R.drawable.ic_like_darkgreen_nofill)
        }

        // Установка ресурса drawable для кнопки
        likeButton.background = drawable

        likeButton.setPadding(-5, 5, 0, 0)
    }

    inner class LikeButtonHandler(private val dbHelper: SQLiteOpenHelper, private val likeButton: Button) {

        fun handleLikeButtonClick(productId: Int, userId: String) {
            val isLiked = isProductLiked(productId, userId)
            if (isLiked) {
                unlikeProduct(productId, userId)
                likeButton.setBackgroundResource(R.drawable.ic_like_darkgreen_nofill)
            } else {
                likeProduct(productId, userId)
                likeButton.setBackgroundResource(R.drawable.ic_like_darkgreen_fill)
            }
        }

        private fun isProductLiked(productId: Int, userId: String): Boolean {
            val db = dbHelper.readableDatabase
            val query = "SELECT COUNT(*) FROM LikesClient WHERE Products_id = ? AND Clients_id = ?"
            val cursor = db.rawQuery(query, arrayOf(productId.toString(), userId))
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()
            return count > 0
        }

        private fun likeProduct(productId: Int, userId: String) {
            val db = dbHelper.writableDatabase
            val query = "INSERT INTO LikesClient (Products_id, Clients_id) VALUES (?, ?)"
            db.execSQL(query, arrayOf(productId.toString(), userId))
        }

        private fun unlikeProduct(productId: Int, userId: String) {
            val db = dbHelper.writableDatabase
            val query = "DELETE FROM LikesClient WHERE Products_id = ? AND Clients_id = ?"
            db.execSQL(query, arrayOf(productId.toString(), userId))
        }
    }

    fun checkProductInCart(database: SQLiteDatabase, productId: Int, userId: String, addToCartFrameLayout: FrameLayout, addToCartMoreFrameLayout: FrameLayout, quantityTextView: TextView, totalPriceTextView: TextView) {
        val query = "SELECT * FROM CartClient WHERE Clients_id = ? AND Products_id = ?"
        val selectionArgs = arrayOf(userId, productId.toString())
        val cursor = database.rawQuery(query, selectionArgs)

        val productExistsInCart: Boolean = cursor.count > 0
        cursor.close()

        if (productExistsInCart) {
            addToCartFrameLayout.visibility = View.GONE
            addToCartMoreFrameLayout.visibility = View.VISIBLE

            // Показываем количество товара в корзине и вычисляем общую стоимость
            val quantityQuery = "SELECT Quantity FROM CartClient WHERE Clients_id = ? AND Products_id = ?"
            val quantityCursor = database.rawQuery(quantityQuery, arrayOf(userId, productId.toString()))
            quantityCursor.moveToFirst()
            val quantity = quantityCursor.getInt(0)
            quantityCursor.close()
            quantityTextView.text = "$quantity шт"

            val priceQuery = "SELECT Price FROM Products WHERE ID = ?"
            val priceCursor = database.rawQuery(priceQuery, arrayOf(productId.toString()))
            priceCursor.moveToFirst()
            val price = priceCursor.getInt(0)
            priceCursor.close()
            val totalPrice = price * quantity
            totalPriceTextView.text = "$totalPrice ₽"
        } else {
            addToCartFrameLayout.visibility = View.VISIBLE
            addToCartMoreFrameLayout.visibility = View.GONE
        }
    }

    inner class CartButtonHandler(private val dbHelper: SQLiteOpenHelper, private val addToCartFrameLayout: FrameLayout, private val addToCartMoreFrameLayout: FrameLayout, private val quantityTextView: TextView, private val totalPriceTextView: TextView) {

        fun handleAddToCartClick(productId: Int, userId: String) {
            val isProductInCart = isProductInCart(productId, userId)
            if (isProductInCart) {
                updateCartQuantity(productId, userId, 1)
            } else {
                addToCart(productId, userId)
            }
            checkProductInCart(dbHelper.writableDatabase, productId, userId, addToCartFrameLayout, addToCartMoreFrameLayout, quantityTextView, totalPriceTextView)
        }

        fun handleAddMoreClick(productId: Int, userId: String) {
            updateCartQuantity(productId, userId, 1)
            checkProductInCart(dbHelper.writableDatabase, productId, userId, addToCartFrameLayout, addToCartMoreFrameLayout, quantityTextView, totalPriceTextView)
        }

        fun handleDeleteClick(productId: Int, userId: String) {
            val currentQuantity = getCurrentCartQuantity(productId, userId)
            if (currentQuantity > 1) {
                updateCartQuantity(productId, userId, -1)
            } else {
                removeFromCart(productId, userId)
            }
            checkProductInCart(dbHelper.writableDatabase, productId, userId, addToCartFrameLayout, addToCartMoreFrameLayout, quantityTextView, totalPriceTextView)
        }

        private fun isProductInCart(productId: Int, userId: String): Boolean {
            val db = dbHelper.readableDatabase
            val query = "SELECT COUNT(*) FROM CartClient WHERE Products_id = ? AND Clients_id = ?"
            val cursor = db.rawQuery(query, arrayOf(productId.toString(), userId))
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()
            return count > 0
        }

        private fun addToCart(productId: Int, userId: String) {
            val db = dbHelper.writableDatabase
            val query = "INSERT INTO CartClient (Clients_id, Products_id, Quantity) VALUES (?, ?, 1)"
            db.execSQL(query, arrayOf(userId, productId))
        }

        private fun updateCartQuantity(productId: Int, userId: String, quantityChange: Int) {
            val db = dbHelper.writableDatabase
            val query = "UPDATE CartClient SET Quantity = Quantity + ? WHERE Clients_id = ? AND Products_id = ?"
            db.execSQL(query, arrayOf(quantityChange, userId, productId))
        }

        private fun removeFromCart(productId: Int, userId: String) {
            val db = dbHelper.writableDatabase
            val query = "DELETE FROM CartClient WHERE Clients_id = ? AND Products_id = ?"
            db.execSQL(query, arrayOf(userId, productId))
        }

        private fun getCurrentCartQuantity(productId: Int, userId: String): Int {
            val db = dbHelper.readableDatabase
            val query = "SELECT Quantity FROM CartClient WHERE Products_id = ? AND Clients_id = ?"
            val cursor = db.rawQuery(query, arrayOf(productId.toString(), userId))
            cursor.moveToFirst()
            val quantity = cursor.getInt(0)
            cursor.close()
            return quantity
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProductFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProductFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}