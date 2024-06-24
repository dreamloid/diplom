import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.DatabaseHelper
import com.example.diplom.adapter.Product
import com.example.diplom.databinding.ItemCardLayoutBinding
import com.example.diplom.db.UserManager

class ProductAdapter(private val context: Context, private val productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var onProductClickListener: OnProductClickListener? = null
    private var onAddToCartClickListener: OnAddToCartClickListener? = null

    private var onAddProductClickListener: OnAddProductClickListener? = null
    private var onDeleteProductClickListener: OnDeleteProductClickListener? = null

    inner class ProductViewHolder(private val binding: ItemCardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val textViewId = binding.textViewID
        val imageViewProduct = binding.imageViewProduct
        val textViewTitle = binding.textViewTitle
        val textViewVolume = binding.textViewProductVolume
        val textViewPrice = binding.textViewPrice
        val textViewTitleEng = binding.textViewTitleEng
        val firstProductFrameLayout = binding.firstProductFrameLayout
        val addMoreProductFrameLayout = binding.addMoreProductFrameLayout
        val quantityTextView = binding.quantityTextView

        val buttonAddCart = binding.buttonAddCart
        val buttonAddProduct = binding.addProduct //добавить еще
        val buttonDeleteProduct = binding.deleteProduct // удалить


        init {
            buttonAddCart.setOnClickListener {
                val productId = productList[adapterPosition].ID
                onAddToCartClickListener?.onAddToCartClick(productId)
            }

            buttonAddProduct.setOnClickListener{
                val productId = productList[adapterPosition].ID
                onAddProductClickListener?.onAddProductClick(productId)
            }

            buttonDeleteProduct.setOnClickListener{
                val productId = productList[adapterPosition].ID
                onDeleteProductClickListener?.onDeleteProductClick(productId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemCardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.textViewId.text = product.ID.toString()
        holder.textViewTitle.text = product.Title
        holder.textViewVolume.text = product.Volume
        holder.textViewPrice.text = product.Price
        holder.textViewTitleEng.text = product.TitleEng

        // Получаем количество товара в корзине
        val productId = product.ID
        val quantity = getQuantityInCart(productId)

        // Устанавливаем количество в quantityTextView
        holder.quantityTextView.text = quantity

        val resourceId = context.resources.getIdentifier(product.ImageName, "drawable", context.packageName)
        if (resourceId != 0) {
            holder.imageViewProduct.setImageResource(resourceId)
        }

        // Получение информации о том, добавлен ли продукт в корзину
        val isInCart = checkIfProductInCart(product.ID)

        // Изменяем видимость элементов в зависимости от наличия продукта в корзине
        if (isInCart) {
            holder.firstProductFrameLayout.visibility = View.GONE
            holder.addMoreProductFrameLayout.visibility = View.VISIBLE
        } else {
            holder.firstProductFrameLayout.visibility = View.VISIBLE
            holder.addMoreProductFrameLayout.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val productId = productList[position].ID // Получаем ID продукта
            onProductClickListener?.onProductClick(productId)
        }
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


    override fun getItemCount(): Int {
        return productList.size
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

    fun setOnProductClickListener(listener: OnProductClickListener) {
        this.onProductClickListener = listener
    }

    fun setOnAddToCartClickListener(listener: OnAddToCartClickListener) {
        this.onAddToCartClickListener = listener
    }

    fun setOnAddProductClickListener(listener: OnAddProductClickListener) {
        this.onAddProductClickListener = listener
    }

    fun setOnDeleteProductClickListener(listener: OnDeleteProductClickListener) {
        this.onDeleteProductClickListener = listener
    }
}
