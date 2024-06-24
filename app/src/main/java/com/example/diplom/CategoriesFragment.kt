package com.example.diplom

import ProductAdapter
import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.diplom.databinding.FragmentCategoriesBinding
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.example.diplom.adapter.Product
import com.example.diplom.db.CategoryManager
import com.example.diplom.db.ProductManager
import com.example.diplom.db.UserManager
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoriesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CategoriesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var categoryId: Int? = null

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var productAdapter: ProductAdapter
    private var productList: List<Product> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getInt(ARG_CATEGORY_ID, -1)
        }

        CategoryManager.categoryId = categoryId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        val view = binding.root

        // Получаем доступ к базе данных и заполняем RecyclerView данными
        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase
        this.productList = getProductsFromDatabase(database)

        productAdapter = ProductAdapter(requireContext(), productList)
        binding.recyclerViewProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewProducts.adapter = productAdapter

        productAdapter.setOnAddToCartClickListener(object : ProductAdapter.OnAddToCartClickListener {
            override fun onAddToCartClick(productId: Int) {
                addToCart(productId)

                productAdapter.notifyDataSetChanged()
            }
        })

        productAdapter.setOnAddProductClickListener(object : ProductAdapter.OnAddProductClickListener {
            override fun onAddProductClick(productId: Int) {
                addToCartMore(productId)

                productAdapter.notifyDataSetChanged()
            }
        })

        productAdapter.setOnDeleteProductClickListener(object : ProductAdapter.OnDeleteProductClickListener {
            override fun onDeleteProductClick(productId: Int) {
                deleteToCart(productId)

                productAdapter.notifyDataSetChanged()
            }
        })

        val categoryTitle = view.findViewById<TextView>(R.id.categoriesNameTextView)

        getCategoryName(database, categoryTitle)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = view.findViewById<TextView>(R.id.CategiryIdTextView)
        categoryId.text = CategoryManager.categoryId.toString()

        // Находим кнопку arrowIconImageView
        val arrowIconImageView = view.findViewById<ImageView>(R.id.arrowIconImageView)

        // Подключаем обработчик нажатия на иконку
        arrowIconImageView.setOnClickListener {
            Log.d("CategoriesFragment", "arrowIconImageView clicked")
            val catalogFragment = CatalogFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_container, catalogFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        productAdapter.setOnProductClickListener(object : ProductAdapter.OnProductClickListener {
            override fun onProductClick(productId: Int) {
                ProductManager.productId = productId
                val args = Bundle().apply {putInt("previousFragmentId", 1)}
                val fragment = ProductFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        })
    }

    // Функция для получения списка товаров из базы данных
    fun getProductsFromDatabase(database: SQLiteDatabase): List<Product> {
        val productList = mutableListOf<Product>()

        val query = "select Products.ID, Title, Volume, Price, TitleEng, Abbreviation from Products, UnitMeasurements where UnitMeasurements.ID = Products.UnitMeasurements_id and Products.ProductTypes_id = ${CategoryManager.categoryId}"
        val cursor = database.rawQuery(query, null)

        // Проверяем, что результирующий набор не пустой
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))
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
                val product = Product(id, title, totalVolume, totalPrice, imageName, titleEng)
                productList.add(product)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return productList
    }

    private fun addToCart(productId: Int) {
        // Получите доступ к базе данных и выполните операцию добавления товара в корзину
        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.writableDatabase

        // Получите ID клиента из UserManager
        val clientId = UserManager.userId

        // Вставьте запись в таблицу "CartClient"
        val contentValues = ContentValues().apply {
            put("Clients_id", clientId)
            put("Products_id", productId)
            put("Quantity", 1) // Начальное количество, можете изменить по своему усмотрению
        }
        database.insert("CartClient", null, contentValues)

        // Оповестите пользователя об успешном добавлении товара в корзину (опционально)
        //Toast.makeText(requireContext(), "Товар добавлен в корзину", Toast.LENGTH_SHORT).show()
    }

    private fun addToCartMore(productId: Int) {
        // Получаем доступ к базе данных
        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.writableDatabase

        // Получаем ID клиента из UserManager
        val clientId = UserManager.userId

        // Проверяем, существует ли уже запись в корзине для данного продукта и клиента
        val query = "SELECT * FROM CartClient WHERE Clients_id = $clientId AND Products_id = $productId"
        val cursor = database.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            // Если запись уже существует, увеличиваем количество
            val currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))
            val newQuantity = currentQuantity + 1

            // Обновляем количество в базе данных
            val values = ContentValues().apply {
                put("Quantity", newQuantity)
            }
            val whereClause = "Clients_id = ? AND Products_id = ?"
            val whereArgs = arrayOf(clientId.toString(), productId.toString())
            database.update("CartClient", values, whereClause, whereArgs)
        } else {
            // Если запись не существует, создаем новую запись
            val values = ContentValues().apply {
                put("Clients_id", clientId)
                put("Products_id", productId)
                put("Quantity", 1)
            }
            database.insert("CartClient", null, values)
        }

        // Закрываем курсор
        cursor?.close()
    }

    private fun deleteToCart(productId: Int) {
        // Получаем доступ к базе данных
        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.writableDatabase

        // Получаем ID клиента из UserManager
        val clientId = UserManager.userId

        // Проверяем, существует ли запись в корзине для данного продукта и клиента
        val query = "SELECT * FROM CartClient WHERE Clients_id = $clientId AND Products_id = $productId"
        val cursor = database.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))

            // Если количество продукта больше одного, уменьшаем его на один
            if (currentQuantity > 1) {
                val newQuantity = currentQuantity - 1
                val values = ContentValues().apply {
                    put("Quantity", newQuantity)
                }
                val whereClause = "Clients_id = ? AND Products_id = ?"
                val whereArgs = arrayOf(clientId.toString(), productId.toString())
                database.update("CartClient", values, whereClause, whereArgs)
            } else {
                // Если количество продукта равно одному, удаляем запись из базы данных
                val whereClause = "Clients_id = ? AND Products_id = ?"
                val whereArgs = arrayOf(clientId.toString(), productId.toString())
                database.delete("CartClient", whereClause, whereArgs)
            }
        }

        // Закрываем курсор
        cursor?.close()
    }

    fun getCategoryName(database: SQLiteDatabase,
                        Title: TextView) {
        val query = "select Title from ProductTypes where ID = ${CategoryManager.categoryId}"
        val cursor = database.rawQuery(query, null)
        if(cursor.moveToFirst()) {
            Title.text = cursor.getString(cursor.getColumnIndexOrThrow("Title"))
        }
        cursor.close()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param categoryId  Parameter 1.
         * @return A new instance of fragment CategoriesFragment.
         */

        private const val ARG_CATEGORY_ID = "categoryId"

        fun newInstance(categoryId: Int) =
            CategoriesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CATEGORY_ID, categoryId)
                }
            }
    }
}