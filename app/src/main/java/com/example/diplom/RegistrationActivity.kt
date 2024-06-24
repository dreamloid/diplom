package com.example.diplom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Properties
import java.util.Random
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class RegistrationActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        databaseHelper = DatabaseHelper(this)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Закрытие базы данных перед уничтожением активности
        databaseHelper.close()
    }
    fun onRegistrationButtonClick(view: View) {
        val generatedCode = generateSixDigitCode()
        val firstName = findViewById<EditText>(R.id.firstNameText).text.toString()
        val lastName = findViewById<EditText>(R.id.lastNameText).text.toString()
        val emailEditText = findViewById<EditText>(R.id.emailText)
        val email = emailEditText.text.toString()
        val password = findViewById<EditText>(R.id.passwordText).text.toString()
        val repeatPassword = findViewById<EditText>(R.id.repeatpasswordText).text.toString()

        val emailPattern = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")

        if (password == repeatPassword) {
            if (!databaseHelper.isEmailExists(email)) {
                if (email.matches(emailPattern)){
                    val intent = Intent(this, VerificationActivity::class.java)
                    intent.putExtra("enteredFirstName", firstName)
                    intent.putExtra("enteredLastName", lastName)
                    intent.putExtra("enteredEmail", email)
                    intent.putExtra("enteredPassword", password)
                    intent.putExtra("generatedCode", generatedCode)
                    startActivity(intent)
                    // Запускам корутину для выполнения асинхронной операции отправки письма
                    GlobalScope.launch(Dispatchers.IO) {
                        sendEmail(email, generatedCode)
                    }
                }
                else {
                    Toast.makeText(this, "Пожалуйста, введите правильный email", Toast.LENGTH_SHORT).show()
                    emailEditText.requestFocus()
                }
            }
            else {
                // Вывести сообщение, что email уже существует в базе
                Toast.makeText(this, "Пользователь с таким email уже зарегистрирован", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            // Вывести сообщение, что пароли не совпадают
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateSixDigitCode(): String {
        val random = Random()
        val codeBuilder = StringBuilder()

        for (i in 1..6) {
            val digit = random.nextInt(10)
            codeBuilder.append(digit)
        }

        return codeBuilder.toString()
    }

    private suspend fun sendEmail(recipient: String, code: String) {
        try {
            val properties = Properties()
            properties["mail.smtp.host"] = "smtp.mail.ru"
            properties["mail.smtp.port"] = "587"
            properties["mail.smtp.auth"] = "true"
            properties["mail.smtp.starttls.enable"] = "true"

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("diplomcompany@mail.ru", "ajFyLVhbvJGVXuH8dMrL")
                }
            })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress("diplomcompany@mail.ru"))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            message.subject = "Подтверждение регистрации"
            message.setText("Ваш проверочный код: $code")

            Transport.send(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onSwitchToLoginButtonClick(view: View) {
        val intent = Intent(this@RegistrationActivity, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}