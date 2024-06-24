package com.example.diplom

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent

class AddBankCardActivity : AppCompatActivity() {

    private lateinit var streetEditText: EditText
    private lateinit var houseEditText: EditText
    private lateinit var floorEditText: EditText
    private lateinit var flatEditText: EditText
    private lateinit var doorphoneEditText: EditText
    private lateinit var commentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_bank_card)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        streetEditText = findViewById(R.id.streetEditText)
        houseEditText = findViewById(R.id.houseEditText)
        floorEditText = findViewById(R.id.floorEditText)
        flatEditText = findViewById(R.id.flatEditText)
        doorphoneEditText = findViewById(R.id.doorphoneEditText)
        commentEditText = findViewById(R.id.commentEditText)

    }

    fun onAddCardButtonClick(view: View) {
        val street = streetEditText.text.toString()
        val house = houseEditText.text.toString()
        val flat = flatEditText.text.toString()

        if (street.isNotBlank() && house.isNotBlank() && flat.isNotBlank()) {
            // Создаем экземпляр класса DatabaseHelper
            val dbHelper = DatabaseHelper(this)

            // Получаем значения из EditText
            val city = "Екатеринбург"
            val houseValue = house.toIntOrNull() ?: 0
            val flatValue = flat.toIntOrNull() ?: 0
            val floor = floorEditText.text.toString().toIntOrNull()
            val doorphone = doorphoneEditText.text.toString()
            val comment = commentEditText.text.toString()

            // Вставляем данные в базу данных
            dbHelper.saveDeliveryAddress(city, street, houseValue, flatValue, floor, doorphone, comment)

            // Опционально: показываем сообщение об успешном добавлении
            Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, BankCardActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Список для хранения названий незаполненных полей
            val missingFields = mutableListOf<String>()

            if (street.isBlank()) missingFields.add("улицу")
            if (house.isBlank()) missingFields.add("дом")
            if (flat.isBlank()) missingFields.add("квартиру")

            // Формируем сообщение о незаполненных полях
            val message = "Заполните ${missingFields.joinToString(" и ")}"

            // Показываем сообщение об ошибке
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onExitButtonClick(view: View) {
        val intent = Intent(this, BankCardActivity::class.java)
        startActivity(intent)
        finish()
    }
}