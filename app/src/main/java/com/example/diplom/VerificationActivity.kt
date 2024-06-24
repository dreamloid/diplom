package com.example.diplom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.diplom.db.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class VerificationActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var countDownTimer: CountDownTimer
    private var combinedCode:String = ""
    val timerDuration = 60000 // длительность таймера в миллисекундах (1 минута)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val enteredFirstName = intent.getStringExtra("enteredFirstName") ?: ""
        val enteredLastName = intent.getStringExtra("enteredLastName") ?: ""
        val enteredEmail = intent.getStringExtra("enteredEmail") ?: ""
        val enteredPassword = intent.getStringExtra("enteredPassword") ?: ""
        val generatedCode = intent.getStringExtra("generatedCode") ?: ""

        val firstCharacter = findViewById<EditText>(R.id.firstCharacter)
        val secondCharacter = findViewById<EditText>(R.id.secondCharacter)
        val thirdCharacter = findViewById<EditText>(R.id.thirdCharacter)
        val fourthCharacter = findViewById<EditText>(R.id.fourthCharacter)
        val fifthCharacter = findViewById<EditText>(R.id.fifthCharacter)
        val sixthCharacter = findViewById<EditText>(R.id.sixthCharacter)
        val registrationButton = findViewById<Button>(R.id.registrationButton)
        firstCharacter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    secondCharacter.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        secondCharacter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    thirdCharacter.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        thirdCharacter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    fourthCharacter.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        fourthCharacter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    fifthCharacter.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        fifthCharacter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    sixthCharacter.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        sixthCharacter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //скрыть клаву
                if (!s.isNullOrEmpty()) {
                    combinedCode = "${firstCharacter.text}${secondCharacter.text}${thirdCharacter.text}${fourthCharacter.text}${fifthCharacter.text}${sixthCharacter.text}"
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(sixthCharacter.windowToken, 0)
                    registrationButton.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        databaseHelper = DatabaseHelper(this)

        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        emailTextView.text = "На почту $enteredEmail было отправлено письмо с кодом регистрации"


        registrationButton.setOnClickListener{
            val enteredCode = combinedCode
            if(enteredCode == generatedCode){
                val insertedId = databaseHelper.saveClient(enteredLastName, enteredFirstName, enteredEmail, enteredPassword, enteredCode)
                UserManager.userId = insertedId.toString()
                saveUserSession(UserManager.userId!!)
                navigateToMainScreen()
                //val intent = Intent(this, MainActivity::class.java)
                //startActivity(intent)
            } else{
                //Toast.makeText(this, combinedCode.toString(), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Неверный код", Toast.LENGTH_SHORT).show()
            }
        }

        // Создаем и запускаем таймер при создании активити
        startTimer()
    }

    private fun startTimer() {
        val senCodeAgainButton = findViewById<Button>(R.id.sendCodeAgainButton)
        countDownTimer = object : CountDownTimer(timerDuration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                // Обновляем текст кнопки с отсчетом времени
                senCodeAgainButton.text = "Повторно отправить через $secondsLeft сек."
                senCodeAgainButton.isEnabled = false // Блокируем кнопку во время таймера
            }

            override fun onFinish() {
                // Разблокируем кнопку и восстанавливаем изначальный текст
                senCodeAgainButton.isEnabled = true
                senCodeAgainButton.text = "Отправить код повторно"
            }
        }.start()
    }

    fun onSendCodeAgainButton(view: View) {
        val enteredEmail = intent.getStringExtra("enteredEmail") ?: ""
        val generatedCode = intent.getStringExtra("generatedCode") ?: ""
        // Запускам корутину для выполнения асинхронной операции отправки письма
        GlobalScope.launch(Dispatchers.IO) {
            sendEmail(enteredEmail, generatedCode)
        }
        startTimer()
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

    private fun saveUserSession(userId: String) {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_id", userId)
        editor.apply()
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}