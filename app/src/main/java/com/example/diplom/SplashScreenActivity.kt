package com.example.diplom

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageView
//import com.bumptech.glide.Glide
import com.example.diplom.db.UserManager

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spashscreen)

//        val loadingImageView = findViewById<ImageView>(R.id.loadingImageView)
//
//        // Загрузка локального GIF с использованием Glide
//        Glide.with(this)
//            .asGif()
//            .load(R.drawable.gif_loading2) // Замените "example" на имя вашего GIF-файла без расширения
//            .into(loadingImageView) // Убедитесь, что ваш ImageView имеет ID yourImageView

        // Загружаем идентификатор пользователя из SharedPreferences
        loadUserSession()

        val timer = object : CountDownTimer(1000, 1000)
        {
            override fun onTick(millisUntilFinished: Long){

            }
            override fun onFinish(){
                if (isUserLoggedIn()) {
                    // Пользователь авторизован, выполнить действия для авторизованного пользователя
                    navigateToMainScreen()
                } else {
                    // Пользователь не авторизован, перейти на экран входа
                    val intent = Intent (this@SplashScreenActivity, AuthenticationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        timer.start()
    }

    private fun loadUserSession() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        UserManager.userId = sharedPreferences.getString("user_id", null)
    }

    private fun isUserLoggedIn(): Boolean {
        return UserManager.userId != null
    }

    private fun navigateToMainScreen() {
        // Ваш код для перехода на главный экран
        val intent = Intent (this@SplashScreenActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}