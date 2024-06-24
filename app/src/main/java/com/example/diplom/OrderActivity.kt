package com.example.diplom

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplom.adapter.Buy
import com.example.diplom.adapter.BuyAdapter
import com.example.diplom.databinding.ActivityOrderBinding
import com.example.diplom.db.OrderManager
import com.example.diplom.db.UserManager
import java.text.SimpleDateFormat
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var buyAdapter: BuyAdapter
    private var buyList: List<Buy> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.binding = binding

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val orderId = findViewById<TextView>(R.id.orderIdTextView)

        orderId.text = OrderManager.orderId.toString()

        val databaseHelper = DatabaseHelper(this)
        val database = databaseHelper.readableDatabase
        this.buyList = getBuyProductsFromDatabase(database)

        buyAdapter = BuyAdapter(this, buyList)
        binding.buyRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.buyRecyclerView.adapter = buyAdapter

        val totalProductCount = calculateTotalProductCount(buyList)
        // Формируем текст в зависимости от количества товаров
        val countProductText = formatProductCount(totalProductCount)
        // Устанавливаем текст в countProductAndVolumeTextView
        binding.countTextView.text = countProductText

        // Рассчитываем финальную цену заказа
        val finalPrice = calculateFinalPrice(buyList)
        // Устанавливаем финальную цену в totalPriceTextView
        binding.totalPriceTextView.text = String.format("%.0f  ₽", finalPrice)
        binding.totalPrice2TextView.text = "При себе необходимо иметь ${String.format("%.0f ₽", finalPrice)}"

        val deliveryAdress = findViewById<TextView>(R.id.deliveryAdressTextView)
        val comment = findViewById<TextView>(R.id.commentEditText)
        val telephone = findViewById<TextView>(R.id.telephoneTextView)
        val dateAndTime = findViewById<TextView>(R.id.dateAndTimeTextView)

        val commentTextView = findViewById<TextView>(R.id.commentTextView)
        val commentEditText = findViewById<TextView>(R.id.commentEditText)
        val View3 = findViewById<View>(R.id.view3)

        getInfoOrder(database, deliveryAdress, commentTextView, commentEditText, View3, telephone, dateAndTime)
    }

    private fun getBuyProductsFromDatabase(database: SQLiteDatabase): List<Buy> {
        val buyList = mutableListOf<Buy>()

        val query = "select Products.TitleEng, OrdersProduct.Quantity, Products.Price, Products.ID " +
                    "from OrdersProduct " +
                    "join Products on OrdersProduct.Products_id = Products.ID " +
                    "join Orders on OrdersProduct.Orders_id = Orders.ID " +
                    "join Clients on Orders.Clients_id = Clients.ID " +
                    "where Clients.ID = ${UserManager.userId} and Orders.ID = ${OrderManager.orderId}"
        val cursor = database.rawQuery(query, null)

        if(cursor.moveToFirst()){
            do{
                val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

                val quanity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))

                val price = cursor.getString(cursor.getColumnIndexOrThrow("Price"))

                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))

                val imageName = "png_$titleEng"
                val buy = Buy(productId, titleEng, imageName, quanity, price)
                buyList.add(buy)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return buyList
    }

    private fun formatProductCount(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count товар"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count товара"
            else -> "$count товаров"
        }
    }

    private fun calculateTotalProductCount(buyList: List<Buy>): Int {
        var totalCount = 0
        for (buy in buyList) {
            totalCount += buy.Quantity
        }
        return totalCount
    }

    private fun calculateFinalPrice(buyList: List<Buy>): Double {
        var totalPrice = 0.0
        for (buy in buyList) {
            // Удаляем символ "₽" из строки с ценой и преобразуем оставшуюся строку в число
            val priceString = buy.Price.replace(" ₽", "")
            val price = priceString.toDouble()
            totalPrice += price * buy.Quantity
        }
        return totalPrice
    }

    fun getInfoOrder(database: SQLiteDatabase,
                     DeliveryAdress: TextView,
                     CommentTextView: TextView,
                     Comment: TextView,
                     View3: View,
                     Telephone: TextView,
                     DateAndTime: TextView){
        val query = "select Orders.Date, Orders.Time, DeliveryAdresses.City, DeliveryAdresses.Street, DeliveryAdresses.House, DeliveryAdresses.Flat, DeliveryAdresses.Comment, Clients.Telephone " +
                    "from DeliveryAdresses " +
                    "join Orders on DeliveryAdresses.ID = Orders.DeliveryAdresses_id " +
                    "join Clients on Orders.Clients_id = Clients.ID " +
                    "where Orders.ID = ${OrderManager.orderId} and Clients.ID = ${UserManager.userId}"
        val cursor = database.rawQuery(query, null)
        if(cursor.moveToFirst()){
            val city = cursor.getString(cursor.getColumnIndexOrThrow("City"))
            val street = cursor.getString(cursor.getColumnIndexOrThrow("Street"))
            val house = cursor.getString(cursor.getColumnIndexOrThrow("House"))
            val flat = cursor.getString(cursor.getColumnIndexOrThrow("Flat"))

            val comment = cursor.getString(cursor.getColumnIndexOrThrow("Comment"))

            val telephone = cursor.getString(cursor.getColumnIndexOrThrow("Telephone"))

            val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("Date"))

            val timeStr = cursor.getString(cursor.getColumnIndexOrThrow("Time"))

            DeliveryAdress.text = "$city, $street, д. $house, кв. $flat"

            // Установка значения для поля Comment
            if (comment.isNullOrEmpty()) {
                CommentTextView.visibility = View.GONE
                Comment.visibility = View.GONE
                View3.visibility = View.GONE
            } else {
                Comment.text = comment
                CommentTextView.visibility = View.VISIBLE
                Comment.visibility = View.VISIBLE
                View3.visibility = View.VISIBLE
            }

            // Установка значения для поля Telephone
            Telephone.text = telephone ?: "не указан"

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse("$dateStr $timeStr")

            // Форматируем дату и время в требуемый формат "31 янв. в 23:59"
            val formattedDate = SimpleDateFormat("dd MMM 'в' HH:mm", Locale("ru")).format(date)

            DateAndTime.text = formattedDate?: "не указан"
        }
    }

    fun onExitButtonClick(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("FROM_ORDER_ACTIVITY", true)
        startActivity(intent)
    }

    fun onReviewButtonClick(view: View) {
        val intent = Intent(this, ChoiceReviewActivity::class.java)
        //val intent = Intent(this, ReviewActivity::class.java)
        // Передача списка ID товаров в ReviewActivity
        //val productIdList = buyList.map { it.ProductId }.toIntArray()
        //intent.putExtra("productIds", productIdList)
        startActivity(intent)
    }

    fun onTelegramButtonClick(view: View) {
        val telegramUsername = "dreamloid"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://t.me/$telegramUsername")
        startActivity(intent)
    }
    fun onTelephoneButtonClick(view: View) {
        val phoneNumber = "89521423252"
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }
}