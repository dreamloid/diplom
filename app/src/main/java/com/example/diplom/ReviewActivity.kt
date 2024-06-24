package com.example.diplom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.diplom.db.OrderManager
import com.example.diplom.db.ProductManager
import com.example.diplom.DatabaseHelper
import com.example.diplom.db.UserManager

class ReviewActivity : AppCompatActivity() {

    private lateinit var dignities: EditText
    private lateinit var disadvantages: EditText
    private lateinit var comment: EditText
    private var selectedStar: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_review)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var selectedStars = intent.getIntExtra("selectedStars", 0)
        selectedStar = selectedStars
        val starCount = findViewById<TextView>(R.id.starCount)
        starCount.text = "$selectedStar"

        val productIdTextView = findViewById<TextView>(R.id.productIdTextView)
        val orderIdTextView = findViewById<TextView>(R.id.orderIdTextView)

        productIdTextView.text = "${ProductManager.productId}"
        orderIdTextView.text = "${OrderManager.orderId}"

        dignities = findViewById(R.id.degnitiesEditText)
        disadvantages = findViewById(R.id.disadvantagesEditText)
        comment = findViewById(R.id.commentEditText)

        val productImageView: ImageView = findViewById(R.id.productImageView)

        val titleEng = intent.getStringExtra("titleEng")
        val productId = intent.getStringExtra("productId")
        val imageName = "png_$titleEng"
        val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
        if (resourceId != 0) {
            productImageView.setImageResource(resourceId)
        } else {
            // Если изображение не найдено, можно установить заглушку или другое изображение по умолчанию
            //productImageView.setImageResource(R.drawable.default_image)
        }

        // Получение списка ID товаров из Intent
        val productIdList = intent.getIntArrayExtra("productIds")

        // Проверка, не является ли список пустым
        if (productIdList != null && productIdList.isNotEmpty()) {
            // Теперь у вас есть список ID товаров, с которыми вы можете работать
            // Например, вы можете использовать этот список для получения информации о товарах
            // или отображения соответствующих элементов интерфейса
            for (productId in productIdList) {
                Log.d("ReviewActivity", "Product ID: $productId")

            }
        } else {
            // Обработка случая, когда список пуст или отсутствует
            Log.e("ReviewActivity", "Product ID list is empty or null")
        }

        val starImageViews: Array<ImageView> = arrayOf(
            findViewById(R.id.star1ImageView),
            findViewById(R.id.star2ImageView),
            findViewById(R.id.star3ImageView),
            findViewById(R.id.star4ImageView),
            findViewById(R.id.star5ImageView)
        )

        // Установка соответствующего количества звезд
        for (i in 0 until selectedStars) {
            starImageViews[i].setImageResource(R.drawable.ic_star_darkgreen)
        }

        // Обработчик клика на звезды
        starImageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                // Сначала сбрасываем все ImageView до исходного состояния
                starImageViews.forEach { it.setImageResource(R.drawable.ic_star_dirtygreen) }

                // Затем изменяем src для всех ImageView до нажатого включительно
                for (i in 0..index) {
                    starImageViews[i].setImageResource(R.drawable.ic_star_darkgreen)
                }

                // Записываем значение в переменную (индекс + 1)
                selectedStar = index + 1
            }
        }



    }
    fun onSendReviewButtonClick(view: View) {
        val productId = intent.getIntExtra("productId", -1)
        val dignitiesText = dignities.text.toString()
        val disadvantagesText = disadvantages.text.toString()
        val commentText = comment.text.toString()

        updateSelectedStar() // Обновляем количество выбранных звезд

        if (productId != -1 && selectedStar != 0) {
            val dbHelper = DatabaseHelper(this) // Создайте экземпляр DatabaseHelper
            dbHelper.saveReview(
                productId,
                selectedStar,
                dignitiesText,
                disadvantagesText,
                commentText
            )

            Toast.makeText(this, "Отзыв успешно отправлен", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ChoiceReviewActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Не удалось отправить отзыв. Проверьте введенные данные", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSelectedStar() {
        // Метод для обновления значения переменной selectedStar
        val starImageViews: Array<ImageView> = arrayOf(
            findViewById(R.id.star1ImageView),
            findViewById(R.id.star2ImageView),
            findViewById(R.id.star3ImageView),
            findViewById(R.id.star4ImageView),
            findViewById(R.id.star5ImageView)
        )

        starImageViews.forEachIndexed { index, imageView ->
            if (imageView.drawable.constantState == ContextCompat.getDrawable(this, R.drawable.ic_star_darkgreen)?.constantState) {
                // Если изображение звезды равно изображению выбранной звезды, устанавливаем значение selectedStar
                selectedStar = index + 1
            }
        }
    }


    fun onExitButtonClick(view: View) {
        onBackPressed()
    }
}