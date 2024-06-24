package com.example.diplom

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.diplom.db.UserManager

class SignInActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        databaseHelper = DatabaseHelper(this)
    }

    fun onAuthorizationButtonClick(view: View) {
        val emailField = findViewById<EditText>(R.id.emailText)
        val passwordField = findViewById<EditText>(R.id.passwordText)

        val email = emailField.text.toString()
        val password = passwordField.text.toString()

        // Проверка наличия email и пароля
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_LONG).show()
            return
        }

        // Проверка аутентификации пользователя
        if (databaseHelper.authenticateUser(email, password)) {
            // Пользователь успешно авторизован
            UserManager.userId?.let {
                saveUserSession(it)
                val intent = Intent(this@SignInActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } ?: run {
                Toast.makeText(this, "Ошибка: User ID is null", Toast.LENGTH_LONG).show()
            }
        } else {
            // Неверный email или пароль
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_LONG).show()

            // Очистка полей после ошибки
            emailField.text.clear()
            passwordField.text.clear()
        }
    }

    fun onSwitchToRegistrationButtonClick(view: View) {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveUserSession(userId: String) {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_id", userId)
        editor.apply()
    }

}