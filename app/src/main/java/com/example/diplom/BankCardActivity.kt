package com.example.diplom

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplom.adapter.BankCard
import com.example.diplom.adapter.BankCardAdapter
import com.example.diplom.databinding.ActivityBankcardBinding
import com.example.diplom.db.UserManager

class BankCardActivity : AppCompatActivity() {

    // Компаньон объект для определения тега для логирования
    companion object {
        private const val TAG = "BankCardActivity"
    }

    private lateinit var binding: ActivityBankcardBinding
    private lateinit var bankCardAdapter : BankCardAdapter
    //private var bankCardList: List<BankCard> = emptyList()
    private var bankCardList: MutableList<BankCard> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate() вызван")

        // Инициализация binding
        val binding = ActivityBankcardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.binding = binding

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dataBaseHelper = DatabaseHelper(this)
        val database = dataBaseHelper.readableDatabase
        this.bankCardList = getBankCardFromDatabase(database).toMutableList()

        bankCardAdapter = BankCardAdapter(this, bankCardList)
        binding.BankCardRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.BankCardRecyclerView.adapter = bankCardAdapter
        bankCardAdapter.notifyDataSetChanged()
    }

    //Функция для получения списка банковских карт из базы данных
    fun getBankCardFromDatabase(database: SQLiteDatabase): List<BankCard> {
        val bankCardList = mutableListOf<BankCard>()

        try {
            // Ваш код получения данных из базы данных
            val query = "select DeliveryAdresses.ID, Street, House, Flat " +
                        "from DeliveryAdresses, Clients, ClientsDeliveryAddresses " +
                        "where ClientsDeliveryAddresses.Clients_id = Clients.ID " +
                        "and ClientsDeliveryAddresses.DeliveryAddresses_id = DeliveryAdresses.ID " +
                        "and ClientsDeliveryAddresses.Clients_id = ${UserManager.userId}"
            val cursor = database.rawQuery(query, null)

            if(cursor.moveToFirst()){
                do{
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))
                    val street = cursor.getString(cursor.getColumnIndexOrThrow("Street"))
                    val house = cursor.getInt(cursor.getColumnIndexOrThrow("House"))
                    val flat = cursor.getInt(cursor.getColumnIndexOrThrow("Flat"))
                    val bankCard = BankCard(id, street, house, flat)
                    bankCardList.add(bankCard)
                } while (cursor.moveToNext())
            }
            cursor.close()

            Log.d("BankActivity", "Bank list from database: $bankCardList")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении банковских карт из базы данных: ${e.message}")
        }
        return bankCardList
    }

    fun onAddCardButtonClick(view: View) {
        val intent = Intent(this, AddBankCardActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onExitButtonClick(view: View) {
        onBackPressed()
    }
}