package com.example.diplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.diplom.db.insertData

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val databaseHelper = DatabaseHelper(this)

        // Проверяем, существует ли база данных
        if (!databaseHelper.databaseExists() || databaseHelper.isDatabaseEmpty()) {
            // Если база данных не существует, создаем ее и заполняем данными
            databaseHelper.writableDatabase
            val insertData = insertData(this)
            insertData.fillData()
        } else {
            // Если база данных уже существует, ничего не делаем
        }

    }

    fun openRegistrationActivity(view: View) {
        val intent = Intent(this@AuthenticationActivity, RegistrationActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun openSignInActivity(view: View) {
        val intent = Intent(this@AuthenticationActivity, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}