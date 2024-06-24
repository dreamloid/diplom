package com.example.diplom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.example.diplom.db.UserManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_catalog -> replaceFragment(CatalogFragment())
                R.id.menu_cart ->  {
                    val haveCart = checkUserCart()
                    if(haveCart) {
                        replaceFragment(CartFragment())
                    } else {
                        replaceFragment(CartEmptyFragment())
                    }
                }
                R.id.menu_likes -> {
                    val hasLikes = checkUserLikes()
                    if (hasLikes) {
                        replaceFragment(LikesFragment())
                    } else {
                        replaceFragment(LikesEmptyFragment())
                    }
                }

                R.id.menu_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
        //Добавляем CatalogFragment при старте приложения с addToBackStack
        if (savedInstanceState == null) {
            replaceFragment(CatalogFragment(), addToBackStack = false)
        }

        if (intent.getBooleanExtra("FROM_ORDER_ACTIVITY", false)) {
            val historyOrderFragment = HistoryOrderFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, historyOrderFragment)
                .commit()
        }
    }

    private fun checkUserCart(): Boolean {
        val dbHelper = DatabaseHelper(this)
        val database = dbHelper.readableDatabase

        val query = "select count(*) from CartClient where Clients_id = ?"
        val cursor = database.rawQuery(query, arrayOf(UserManager.userId.toString()))

        cursor.use {
            if(it.moveToFirst()){
                val count = it.getInt(0)
                return count > 0
            }
        }
        return false
    }

    private fun checkUserLikes(): Boolean {
        val dbHelper = DatabaseHelper(this)
        val database = dbHelper.readableDatabase

        val query = "SELECT COUNT(*) FROM LikesClient WHERE Clients_id = ?"
        val cursor = database.rawQuery(query, arrayOf(UserManager.userId.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                val count = it.getInt(0)
                return count > 0
            }
        }
        return false
    }
    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

}
