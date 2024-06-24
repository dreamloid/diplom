package com.example.diplom

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplom.adapter.Buy
import com.example.diplom.adapter.ChoiceReview
import com.example.diplom.adapter.ChoiceReviewAdapter
import com.example.diplom.databinding.ActivityChoiceReviewBinding
import com.example.diplom.db.OrderManager
import com.example.diplom.db.UserManager

class ChoiceReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChoiceReviewBinding
    private lateinit var choiceAdapter: ChoiceReviewAdapter
    private var choiceList: List<ChoiceReview> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val binding = ActivityChoiceReviewBinding.inflate(layoutInflater)
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
        this.choiceList = getBuyProductsFromDatabase(database)

        choiceAdapter = ChoiceReviewAdapter(this, choiceList)
        binding.choiceRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.choiceRecyclerView.adapter = choiceAdapter
    }

    private fun getBuyProductsFromDatabase(database: SQLiteDatabase): List<ChoiceReview> {
        val buyList = mutableListOf<ChoiceReview>()

        val query = "SELECT Products.ID, Products.Title, Products.TitleEng, OrdersProduct.Quantity, Products.Price, Products.ID " +
                    "FROM OrdersProduct " +
                    "JOIN Products ON OrdersProduct.Products_id = Products.ID " +
                    "JOIN Orders ON OrdersProduct.Orders_id = Orders.ID " +
                    "JOIN Clients ON Orders.Clients_id = Clients.ID " +
                    "LEFT JOIN Reviews ON Products.ID = Reviews.Products_id AND Clients.ID = Reviews.Client_id " +
                    "WHERE Clients.ID = ${UserManager.userId} AND Orders.ID = ${OrderManager.orderId} " +
                    "AND Reviews.ID IS NULL"

        val cursor = database.rawQuery(query, null)

        if(cursor.moveToFirst()){
            do{
                val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))

                val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))

                val imageName = "png_$titleEng"
                val choice = ChoiceReview(title, titleEng, productId, imageName)
                buyList.add(choice)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return buyList
    }

    fun onExitButtonClick(view: View) {
        val intent = Intent(this, OrderActivity::class.java)
        startActivity(intent)
        finish()
    }
}