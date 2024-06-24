package com.example.diplom

import Order
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.adapter.CartAdapter
import com.example.diplom.adapter.LikeAdapter
import com.example.diplom.adapter.OrderAdapter
import com.example.diplom.adapter.Product
import com.example.diplom.adapter.Similar
import com.example.diplom.databinding.FragmentCartBinding
import com.example.diplom.db.CategoryManager
import com.example.diplom.db.ProductManager
import com.example.diplom.db.UserManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CartFragment : Fragment(), OrderAdapter.OnOrderUpdateListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter
    private lateinit var orderAdapter: OrderAdapter
    private var cartList: List<Similar> = emptyList()
    private var orderList: MutableList<Order> = mutableListOf()

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
        binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root

        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase

        this.cartList = getCartFromDatabase(database)
        this.orderList = getOrderFromDatabase(database).toMutableList()


        cartAdapter = CartAdapter(requireContext(), cartList)
        binding.cartRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartAdapter

        orderAdapter = OrderAdapter(requireContext(), orderList, database, requireFragmentManager(), this){
            orderList = getOrderFromDatabase(database).toMutableList()
            orderAdapter.notifyDataSetChanged()
        }
        orderAdapter.updateLikedProducts(getLikedProductsFromDatabase(database))
        binding.orderRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.orderRecyclerView.adapter = orderAdapter
        binding.orderRecyclerView.fixNestedScrolling()

        // Убедитесь, что nestedScrollingEnabled = false для RecyclerView
        binding.orderRecyclerView.isNestedScrollingEnabled = false
        binding.cartRecyclerView.isNestedScrollingEnabled = false

        //setRecyclerViewHeightBasedOnItems(binding.orderRecyclerView)

        //orderAdapter.notifyDataSetChanged()

        // Рассчитываем финальную цену заказа
        val finalPrice = calculateFinalPrice(orderList)
        // Устанавливаем финальную цену в totalPriceTextView
        binding.totalPriceTextView.text = String.format("%.0f  ₽", finalPrice)
        binding.totalPrice2TextView.text = String.format("%.0f ₽", finalPrice)
        binding.totalPrice3TextView.text = String.format("%.0f ₽", finalPrice)

        // Рассчитываем общее количество товаров
        val totalProductCount = calculateTotalProductCount(orderList)
        // Формируем текст в зависимости от количества товаров
        val countProductText = formatProductCount(totalProductCount)
        // Устанавливаем текст в countProductAndVolumeTextView
        binding.countProductAndVolumeTextView.text = countProductText

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deleteCartButton = view.findViewById<Button>(R.id.deleteCartButton)
        deleteCartButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        val orderRegistrationFrameLayout = view.findViewById<ConstraintLayout>(R.id.orderRegistrationFrameLayout)
        orderRegistrationFrameLayout.setOnClickListener {
            val finalPrice = calculateFinalPrice(orderList)
            // Создаем Intent для перехода на OrderRegistrationActivity
            val intent = Intent(requireContext(), OrderRegistrationActivity::class.java)
            // Передаем finalPrice как дополнительную информацию в Intent
            intent.putExtra("final_price", finalPrice)
            // Передаем orderList как дополнительную информацию в Intent
            intent.putParcelableArrayListExtra("productList", ArrayList(orderList))
            // Запускаем OrderRegistrationActivity
            startActivity(intent)
        }

        cartAdapter.setOnProductClickListener(object : CartAdapter.OnProductClickListener {
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

        orderAdapter.setOnProductClickListener(object : OrderAdapter.OnProductClickListener {
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

    // Метод для отображения диалогового окна с подтверждением уделеня заказа
    private fun showDeleteConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_cart, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.positiveButton).setOnClickListener {
            // Действие при нажатии на кнопку "Да"
            dialog.dismiss()
            // Переход на экран аутентификации
            deleteCartFromDatabase()
            // Переходим на фрагмент CartEmptyFragment
            val cartEmptyFragment = CartEmptyFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, cartEmptyFragment)
                .addToBackStack(null)
                .commit()
        }
        dialogView.findViewById<Button>(R.id.negativeButton).setOnClickListener {
            // Действие при нажатии на кнопку "Отмена"
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteCartFromDatabase() {
        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.writableDatabase

        val query = "DELETE FROM CartClient WHERE Clients_id = ${UserManager.userId}"
        database.execSQL(query)

        // Здесь вы можете выполнить любые дополнительные действия после удаления корзины, например, обновление интерфейса
    }

    private fun formatProductCount(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count товар"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count товара"
            else -> "$count товаров"
        }
    }

    private fun calculateTotalProductCount(orderList: MutableList<Order>): Int {
        var totalCount = 0
        for (order in orderList) {
            totalCount += order.Quantity
        }
        return totalCount
    }

    private fun calculateFinalPrice(orderList: MutableList<Order>): Double {
        var totalPrice = 0.0
        for (order in orderList) {
            // Удаляем символ "₽" из строки с ценой и преобразуем оставшуюся строку в число
            val priceString = order.Price.replace(" ₽", "")
            val price = priceString.toDouble()
            totalPrice += price * order.Quantity
        }
        return totalPrice
    }

    private fun getLikedProductsFromDatabase(database: SQLiteDatabase): List<String> {
        val likedProducts = mutableListOf<String>()
        val query = "SELECT Products_id FROM LikesClient WHERE Clients_id = ${UserManager.userId}"
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val productId = cursor.getString(cursor.getColumnIndexOrThrow("Products_id"))
                likedProducts.add(productId)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return likedProducts
    }

    private fun getOrderFromDatabase(database: SQLiteDatabase): List<Order> {
        val orderList = mutableListOf<Order>()

        val query = "select Products.ID, Products.Title, Products.Volume, Products.Price, Products.TitleEng, UnitMeasurements.Abbreviation, CartClient.Quantity, " +
                    "case when LikesClient.Products_id is not null then 1 else 0 end as isLiked " +
                    "from Products " +
                    "join UnitMeasurements on Products.UnitMeasurements_id = UnitMeasurements.ID " +
                    "join CartClient on Products.ID = CartClient.Products_id " +
                    "join Clients on CartClient.Clients_id = Clients.ID " +
                    "left join LikesClient on Products.ID = LikesClient.Products_id and LikesClient.Clients_id = Clients.ID " +
                    "where Clients.ID = ${UserManager.userId}"
        val cursor = database.rawQuery(query, null)
        orderList.clear()
        if (cursor.moveToFirst()){
            do{
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))

                val volume = cursor.getDouble(cursor.getColumnIndexOrThrow("Volume"))
                val formattedVolume = String.format("%.0f", volume)
                val unitMeasurements = cursor.getString(cursor.getColumnIndexOrThrow("Abbreviation"))

                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
                val formattedPrice = String.format("%.0f", price)

                val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))

                val imageName = "png_$titleEng"
                val totalPrice = "$formattedPrice ₽"
                val totalVolume = "$formattedVolume $unitMeasurements"

                val isLiked = cursor.getInt(cursor.getColumnIndexOrThrow("isLiked")) == 1

                val order = Order(imageName, productId, title, totalVolume, titleEng, totalPrice, quantity, isLiked)
                orderList.add(order)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orderList
    }

    private fun getCartFromDatabase(database: SQLiteDatabase): List<Similar> {
        val cartList = mutableListOf<Similar>()

        val query = "select Products.ID as ProductID, ProductTypes.ID as ProductTypeID, Products.Title, Volume, Price, TitleEng, Abbreviation " +
                    "from Products, UnitMeasurements, ProductTypes " +
                    "where UnitMeasurements.ID = Products.UnitMeasurements_id " +
                    "and ProductTypes.ID = Products.ProductTypes_id " +
                    "order by random() limit 9"
        val cursor = database.rawQuery(query, null)

        if(cursor.moveToFirst()){
            do{
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ProductID"))

                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("ProductTypeID"))

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
                val cart = Similar(productId, categoryId, title, totalVolume, totalPrice, imageName, titleEng)
                cartList.add(cart)
            } while(cursor.moveToNext())
        }

        cursor.close()
        return cartList
    }

    override fun onOrderUpdated() {
        var totalPrice = 0.0
        var totalQuantity = 0

        // Проходимся по каждому элементу списка
        for (order in orderList) {
            // Удаляем символ "₽" из строки с ценой и преобразуем оставшуюся строку в число
            val priceString = order.Price.replace(" ₽", "")
            val price = priceString.toDouble()

            // Считаем общую цену, умножая цену товара на его количество
            totalPrice += price * order.Quantity

            // Считаем общее количество товаров
            totalQuantity += order.Quantity
        }

        // Обновляем текстовые поля с общей ценой и общим количеством товаров
        binding.totalPriceTextView.text = String.format("%.0f ₽", totalPrice)
        binding.totalPrice2TextView.text = String.format("%.0f ₽", totalPrice)
        binding.totalPrice3TextView.text = String.format("%.0f ₽", totalPrice)

        val countProductText = formatProductCount(totalQuantity)

        binding.countProductAndVolumeTextView.text = countProductText
    }

    fun RecyclerView.fixNestedScrolling() {
        this.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val params = this.layoutParams
            params.height = this.computeVerticalScrollRange()
            this.layoutParams = params
        }
    }

    fun setRecyclerViewHeightBasedOnItems(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        var totalHeight = 0
        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += holder.itemView.measuredHeight
        }
        val params = recyclerView.layoutParams
        params.height = totalHeight
        recyclerView.layoutParams = params
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}