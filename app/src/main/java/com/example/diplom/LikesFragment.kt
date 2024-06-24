package com.example.diplom

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.adapter.Like
import com.example.diplom.adapter.LikeAdapter
import com.example.diplom.databinding.FragmentLikesBinding
import com.example.diplom.db.ProductManager
import com.example.diplom.db.UserManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LikesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LikesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentLikesBinding
    private lateinit var likeAdapter: LikeAdapter
    //private var likeList: List<Like> = emptyList()

    private var likeList: MutableList<Like> =  mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLikesBinding.inflate(inflater, container, false)
        val view = binding.root

        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase
        likeList = getLikeFromDatabase(database).toMutableList()

        likeAdapter = LikeAdapter(requireContext(), likeList, requireFragmentManager())
        binding.likeRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.likeRecyclerView.adapter = likeAdapter

        // Проверяем, если список пуст, перенаправляем пользователя на другой экран

        likeAdapter.notifyDataSetChanged()

        likeAdapter.setOnAddToCartClickListener(object: LikeAdapter.OnAddToCartClickListener{
            override fun onAddToCartClick(productId: Int) {
                addToCart(productId)

                likeAdapter.notifyDataSetChanged()
            }
        })

        likeAdapter.setOnAddProductClickListener(object : LikeAdapter.OnAddProductClickListener {
            override fun onAddProductClick(productId: Int) {
                addToCartMore(productId)

                likeAdapter.notifyDataSetChanged()
            }
        })

        likeAdapter.setOnDeleteProductClickListener(object : LikeAdapter.OnDeleteProductClickListener {
            override fun onDeleteProductClick(productId: Int) {
                deleteToCart(productId)

                likeAdapter.notifyDataSetChanged()
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        likeAdapter.setOnProductClickListener(object : LikeAdapter.OnProductClickListener {
            override fun onProductClick(productId: Int) {
                ProductManager.productId = productId
                val fragment = ProductFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        })
    }

    //Получить лайкнутые товары
    private fun getLikeFromDatabase(database: SQLiteDatabase): List<Like> {
        val likeList = mutableListOf<Like>()

        val query = "select Products.ID, Products.Title, Products.Volume, UnitMeasurements.Abbreviation, Products.Price, TitleEng " +
                    "from LikesClient, Products, Clients, UnitMeasurements " +
                    "where Clients.ID = ${UserManager.userId} " +
                    "and LikesClient.Clients_id = Clients.ID " +
                    "and LikesClient.Products_id = Products.ID " +
                    "and Products.UnitMeasurements_id = UnitMeasurements.ID"
        val cursor = database.rawQuery(query, null)

        if(cursor.moveToFirst()) {
            do{
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))

                val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))

                val volume = cursor.getDouble(cursor.getColumnIndexOrThrow("Volume"))
                val formattedVolume = String.format("%.0f", volume)

                val unitMeasurements = cursor.getString(cursor.getColumnIndexOrThrow("Abbreviation"))

                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
                val formattedPrice = String.format("%.0f", price)

                val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

                val imageName = "png_$titleEng"
                val totalPrice = "$formattedPrice ₽"
                val totalVolume = "$formattedVolume $unitMeasurements"

                val like = Like(productId, title, totalVolume, totalPrice, imageName, titleEng)
                likeList.add(like)
            }while (cursor.moveToNext())
        }

        cursor.close()

        // Выводим содержимое списка в логи для отладки
        Log.d("LikeFragment", "Like list from database: $likeList")

        return likeList
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
        Toast.makeText(requireContext(), "Товар добавлен в корзину", Toast.LENGTH_SHORT).show()
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

    fun RecyclerView.fixNestedScrolling() {
        this.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val params = this.layoutParams
            params.height = this.computeVerticalScrollRange()
            this.layoutParams = params
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LikesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LikesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}