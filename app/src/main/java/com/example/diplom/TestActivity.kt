package com.example.diplom

import ProductAdapter
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.diplom.adapter.Product
import com.example.diplom.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Создаем список товаров (замените этот код на ваш фактический код получения данных из базы данных)
        //val databaseHelper = DatabaseHelper(this) // предположим, что ваш DatabaseHelper имеет контекст this
        //val database = databaseHelper.readableDatabase
        //val productList = getProductsFromDatabase(database) // передаем объект базы данных


        // Инициализируем адаптер и устанавливаем его для RecyclerView
        //productAdapter = ProductAdapter(this, productList)
        //binding.recyclerViewProducts.layoutManager = GridLayoutManager(this, 2)
        //binding.recyclerViewProducts.adapter = productAdapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Функция для получения списка товаров из базы данных (замените этот код на ваш фактический код получения данных из базы данных)
//    fun getProductsFromDatabase(database: SQLiteDatabase): List<Product> {
//        val productList = mutableListOf<Product>()
//
//        val query = "select Title, Volume, Price, TitleEng, Abbreviation from Products, UnitMeasurements where UnitMeasurements.ID = Products.UnitMeasurements_id"
//        val cursor = database.rawQuery(query, null)
//
//        // Проверяем, что результирующий набор не пустой
//        if (cursor.moveToFirst()) {
//            do {
//                val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))
//                val volume = cursor.getDouble(cursor.getColumnIndexOrThrow("Volume"))
//                val formattedVolume = String.format("%.0f", volume)
//                val unitMeasurements = cursor.getString(cursor.getColumnIndexOrThrow("Abbreviation"))
//
//                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
//                val formattedPrice = String.format("%.0f", price)
//                val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))
//
//                // Имя ресурса изображения (заглушка)
//                val imageName = "png_$titleEng" // Предполагаем, что у вас есть ресурсы с именами, соответствующими наименованию товара
//                val totalPrice = "$formattedPrice ₽"
//                val totalVolume = "$formattedVolume $unitMeasurements"
//                val product = Product(title, totalVolume, totalPrice, imageName, titleEng)
//                productList.add(product)
//            } while (cursor.moveToNext())
//        }
//
//        cursor.close()
//        cursor.close()
//        return productList
//    }

    fun convertToEnglishName(name: String): String {
        return name.replace(Regex("[^A-Za-z0-9]"), "_")
    }


    fun displayAllClients() {
        val databaseHelper = DatabaseHelper(this)
        val clients = databaseHelper.getAllClients()

        for (client in clients) {
            val firstName = client.first
            val lastName = client.second
            val email = client.third
            val password = client.fourth
            val code = client.fifth
            // Выводите email и code на экран или в консоль
            println("FirstName $firstName, LastName $lastName, Email: $email, Password: $password Code: $code")
        }
    }

    fun seeClients(view: View) {
        // Создаем список товаров (замените этот код на ваш фактический код получения данных из базы данных)
        val databaseHelper = DatabaseHelper(this) // предположим, что ваш DatabaseHelper имеет контекст this
        val database = databaseHelper.readableDatabase
        //val productList = getProductsFromDatabase(database) // передаем объект базы данных

        // Выводим конвертированные имена товаров в Logcat
        //for (product in productList) {
        //    val convertedName = convertToEnglishName(product.Title)
        //    Log.d("ConvertedName", "Original: ${product.Title}, Converted: $convertedName")
        //}
    }
}