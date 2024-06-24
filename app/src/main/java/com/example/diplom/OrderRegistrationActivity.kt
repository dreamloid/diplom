package com.example.diplom

import Order
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.adapter.BankCard
import com.example.diplom.adapter.ChoiceBankCardAdapter
import com.example.diplom.adapter.ChoiceOverAdapter
import com.example.diplom.adapter.DeliveryAdress
import com.example.diplom.adapter.DeliveryAdressAdapter
import com.example.diplom.adapter.OrderTime
import com.example.diplom.adapter.OrderTimeAdapter
import com.example.diplom.adapter.OverList
import com.example.diplom.databinding.ActivityOrderRegistrationBinding
import com.example.diplom.db.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.collections.ArrayList

class OrderRegistrationActivity : AppCompatActivity(), DeliveryAdressAdapter.OnItemClickListener {

    private lateinit var binding: ActivityOrderRegistrationBinding
    private lateinit var orderTimeAdapter: OrderTimeAdapter
    private lateinit var choiceOverAdapter: ChoiceOverAdapter
    private lateinit var deliveryAdressAdapter: DeliveryAdressAdapter

    private var orderTimeList: MutableList<OrderTime> = mutableListOf()
    private var deliveryList: MutableList<DeliveryAdress> = mutableListOf()

    private lateinit var overFrameLayout: FrameLayout
    private lateinit var choiceOverFrameLayout: FrameLayout
    private lateinit var giftsFrameLayout: ConstraintLayout
    private lateinit var choiceGiftsFrameLayout: FrameLayout
    private lateinit var bankCardFrameLayout: FrameLayout
    private lateinit var choiceBankCardFrameLayout: FrameLayout
    private lateinit var selectedBankCardFrameLayout: FrameLayout
    private lateinit var selectedChoiceOverFrameLayout: FrameLayout
    private lateinit var deliveryFrameLayout: FrameLayout
    private lateinit var choiceDeliveryFrameLayout: FrameLayout
    private lateinit var selectedChoiceDeliveryFrameLayout: FrameLayout
    private lateinit var firstDeliveryAdressFrameLayout: ConstraintLayout
    private lateinit var mainChoiceDeliveryAdressFrameLayout: FrameLayout

    private lateinit var arrowChoiceOver: ImageView
    private lateinit var arrowChoiceGifts: ImageView
    private lateinit var arrowChoiceBankCard: ImageView
    private lateinit var arrowChoiceDelivery: ImageView

    private lateinit var streetEditText: EditText
    private lateinit var houseEditText: EditText
    private lateinit var floorEditText: EditText
    private lateinit var flatEditText: EditText
    private lateinit var doorphoneEditText: EditText
    private lateinit var commentEditText: EditText
    private lateinit var telephoneEditText: EditText
    private lateinit var contactlessDeliveryButton: SwitchCompat

    // Получение текущей даты и времени
    val calendar = Calendar.getInstance()
    val currentDateAndTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(calendar.time)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("OrderRegistrationActivity", "Order time list size: ${orderTimeList.size}")

        val binding = ActivityOrderRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.binding = binding

        // Получение переменной finalPrice из Intent
        val finalPrice = intent.getDoubleExtra("final_price", 0.0)
        var totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextView)
        totalPriceTextView.text = String.format("%.0f  ₽", finalPrice)

        // Инициализация FrameLayout'ов после вызова setContentView
        overFrameLayout = findViewById(R.id.overFrameLayout)
        choiceOverFrameLayout = findViewById(R.id.choiceOverFrameLayout)
        giftsFrameLayout = findViewById(R.id.giftsFrameLayout)
        choiceGiftsFrameLayout = findViewById(R.id.choiceGiftsFrameLayout)
        bankCardFrameLayout = findViewById(R.id.bankCardFrameLayout)
        choiceBankCardFrameLayout = findViewById(R.id.choiceBankCardFrameLayout)
        selectedBankCardFrameLayout = findViewById(R.id.selectedBankCardFrameLayout)
        selectedChoiceOverFrameLayout = findViewById(R.id.selectedChoiceOverFrameLayout)
        deliveryFrameLayout = findViewById(R.id.deliveryFrameLayout)
        choiceDeliveryFrameLayout = findViewById(R.id.choiceDeliveryFrameLayout)
        selectedChoiceDeliveryFrameLayout = findViewById(R.id.selectedChoiceDeliveryFrameLayout)
        firstDeliveryAdressFrameLayout = findViewById(R.id.firstDeliveryAdressFrameLayout)
        mainChoiceDeliveryAdressFrameLayout = findViewById(R.id.mainChoiceDeliveryAdressFrameLayout)

        arrowChoiceOver = findViewById(R.id.arrowChoiceOver)
        arrowChoiceGifts = findViewById(R.id.arrowChoiceGifts)
        arrowChoiceBankCard = findViewById(R.id.arrowChoiceBankCard)
        arrowChoiceDelivery = findViewById(R.id.arrowChoiceDelivery)

        streetEditText = findViewById(R.id.streetEditText)
        houseEditText = findViewById(R.id.houseEditText)
        floorEditText = findViewById(R.id.floorEditText)
        flatEditText = findViewById(R.id.flatEditText)
        doorphoneEditText = findViewById(R.id.doorphoneEditText)
        commentEditText = findViewById(R.id.commentEditText)
        telephoneEditText = findViewById(R.id.telephoneEditText)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dataBaseHelper = DatabaseHelper(this)
        val database = dataBaseHelper.readableDatabase
        this.orderTimeList = getDayAndTimeFromDatabase(database).toMutableList()
        this.deliveryList = getDeliveryAdressFromDatabase(database).toMutableList()

            // Проверяем наличие адреса доставки
            val deliveryAddressExists = checkDeliveryAddressExists(database)

            // Если адреса доставки нет, изменяем видимость FrameLayout'ов
            if (!deliveryAddressExists) {
                firstDeliveryAdressFrameLayout.visibility = View.VISIBLE
                mainChoiceDeliveryAdressFrameLayout.visibility = View.GONE
            } else if (deliveryAddressExists) {
                firstDeliveryAdressFrameLayout.visibility = View.GONE
                mainChoiceDeliveryAdressFrameLayout.visibility = View.VISIBLE
            }

        orderTimeAdapter = OrderTimeAdapter(this, orderTimeList)
        binding.dayAndTimeRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.dayAndTimeRecyclerView.adapter = orderTimeAdapter
        orderTimeAdapter.notifyDataSetChanged()

        // Находим RecyclerView для choiceOver и присваиваем ему адаптер
        choiceOverAdapter = ChoiceOverAdapter(this, OverList().list)
        binding.choiceOverRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.choiceOverRecyclerView.adapter = choiceOverAdapter

        val selectedChoiceOverTextView = findViewById<TextView>(R.id.selectedChoiceOverTextView)
        val choiceOverTextView = findViewById<TextView>(R.id.choiceOverTextView)

        choiceOverAdapter.setSelectedChoiceOverTextView(selectedChoiceOverTextView)
        choiceOverAdapter.setChoiceOverTextView(choiceOverTextView)
        choiceOverAdapter.setSelectedChoiceOverFrameLayout(selectedChoiceOverFrameLayout)
        choiceOverAdapter.setChoiceOverFrameLayout(choiceOverFrameLayout)

        val selectedChoiceDeliveryTextView = findViewById<TextView>(R.id.selectedChoiceDeliveryTextView)
        val choiceDeliveryTextView = findViewById<TextView>(R.id.choiceDeliveryTextView)

        deliveryAdressAdapter = DeliveryAdressAdapter(this, deliveryList)
        binding.choiceDeliveryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.choiceDeliveryRecyclerView.adapter = deliveryAdressAdapter
        deliveryAdressAdapter.notifyDataSetChanged()

        deliveryAdressAdapter.setSelectedChoiceOverTextView(selectedChoiceDeliveryTextView)
        deliveryAdressAdapter.setChoiceDeliveryTextView(choiceDeliveryTextView)
        deliveryAdressAdapter.setSelectedChoiceDeliveryFrameLayout(selectedChoiceDeliveryFrameLayout)
        deliveryAdressAdapter.setChoiceDeliveryFrameLayout(choiceDeliveryFrameLayout)

        deliveryAdressAdapter.setOnItemClickListener(this)

        getInfoUserFromDatabase(database, telephoneEditText)
    }

    //узнать адреса доставки клиента
    private fun getDeliveryAdressFromDatabase(database: SQLiteDatabase): List<DeliveryAdress> {
        val deliveryList = mutableListOf<DeliveryAdress>()
        val query = "select DeliveryAdresses.ID, DeliveryAdresses.Street, DeliveryAdresses.House, DeliveryAdresses.Flat, DeliveryAdresses.Comment " +
                    "from DeliveryAdresses " +
                    "join ClientsDeliveryAddresses on DeliveryAdresses.ID = ClientsDeliveryAddresses.DeliveryAddresses_id " +
                    "where ClientsDeliveryAddresses.Clients_id = ${UserManager.userId}"
        val cursor = database.rawQuery(query, null)
        if(cursor.moveToFirst()){
            do{
                val deliveryId = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))
                val street = cursor.getString(cursor.getColumnIndexOrThrow("Street"))
                val house = cursor.getInt(cursor.getColumnIndexOrThrow("House"))
                val flat = cursor.getInt(cursor.getColumnIndexOrThrow("Flat"))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("Comment"))

                val deliveryAdress = DeliveryAdress(deliveryId, street, house.toString(), flat.toString(), comment)
                deliveryList.add(deliveryAdress)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return deliveryList
    }

    //найти подходящее время
    private fun getDayAndTimeFromDatabase(database: SQLiteDatabase): List<OrderTime> {
        val orderTimeList = mutableListOf<OrderTime>()
        val currentTime = getCurrentTime() // Функция для получения текущего времени
        // Выбираем все записи для сегодняшнего дня
        val todayQuery = """SELECT ID, Day, Time 
                            FROM DeliveryDayAndTime 
                            WHERE Day = 'Сегодня' AND Time > ?
                            ORDER BY Time"""

        // Выбираем все записи для завтрашнего дня
        val tomorrowQuery = """SELECT ID, Day, Time 
                               FROM DeliveryDayAndTime 
                               WHERE Day = 'Завтра'
                               ORDER BY Time"""

        // Выполняем первый запрос
        val todayCursor = database.rawQuery(todayQuery, arrayOf(currentTime))
        if(todayCursor.moveToFirst()){
            do{
                val time_id = todayCursor.getInt(todayCursor.getColumnIndexOrThrow("ID"))
                val day = todayCursor.getString(todayCursor.getColumnIndexOrThrow("Day"))
                val time = todayCursor.getString(todayCursor.getColumnIndexOrThrow("Time"))
                val orderTime = OrderTime(time_id, day, time)
                orderTimeList.add(orderTime)
            } while (todayCursor.moveToNext())
        }
        todayCursor.close()

        // Выполняем второй запрос
        val tomorrowCursor = database.rawQuery(tomorrowQuery, null)
        if(tomorrowCursor.moveToFirst()){
            do{
                val time_id = tomorrowCursor.getInt(tomorrowCursor.getColumnIndexOrThrow("ID"))
                val day = tomorrowCursor.getString(tomorrowCursor.getColumnIndexOrThrow("Day"))
                val time = tomorrowCursor.getString(tomorrowCursor.getColumnIndexOrThrow("Time"))
                val orderTime = OrderTime(time_id, day, time)
                orderTimeList.add(orderTime)
            } while (tomorrowCursor.moveToNext())
        }

        tomorrowCursor.close()
        return orderTimeList
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return sdf.format(currentTime)
    }

    // скрыть фрейм промокода
    fun onGiftsFrameLayoutClick(view: View) {
        val currentVisability = choiceGiftsFrameLayout.visibility

        choiceGiftsFrameLayout.visibility = if (currentVisability == View.VISIBLE) View.GONE else View.VISIBLE

        arrowChoiceGifts.setImageResource(if(currentVisability == View.VISIBLE) R.drawable.ic_arrow2 else R.drawable.ic_arrow5)
    }

    // скрыть фрейм выбора
    fun onChoiceOverFrameLayoutClick(view: View) {
        val currentVisability = choiceOverFrameLayout.visibility

        choiceOverFrameLayout.visibility = if (currentVisability == View.VISIBLE) View.GONE else View.VISIBLE

        arrowChoiceOver.setImageResource(if(currentVisability == View.VISIBLE) R.drawable.ic_arrow2 else R.drawable.ic_arrow5)
    }

    //скрыть фрейм адресов
    fun onChoiceDeliveryFrameLayoutClick(view: View) {
        val currentVisability = choiceDeliveryFrameLayout.visibility

        choiceDeliveryFrameLayout.visibility = if (currentVisability ==View.VISIBLE) View.GONE else View.VISIBLE

        arrowChoiceDelivery.setImageResource(if(currentVisability == View.VISIBLE) R.drawable.ic_arrow2 else R.drawable.ic_arrow5)
    }

    fun onPayButtonClick(view: View) {
        val streetFilled = streetEditText.text.isNotBlank()
        val houseFilled = houseEditText.text.isNotBlank()
        val flatFilled = flatEditText.text.isNotBlank()

        val dayAndTimeRecyclerView: RecyclerView = findViewById(R.id.dayAndTimeRecyclerView)
        val adapter = dayAndTimeRecyclerView.adapter as OrderTimeAdapter

        val dayAndTimeSelected = adapter.selectedItemPosition != null

        val selectedChoiceVisible = selectedChoiceOverFrameLayout.visibility == View.VISIBLE
        val telephoneFilled = telephoneEditText.text.isNotBlank()

        val allStepsCompleted = streetFilled && houseFilled && flatFilled && dayAndTimeSelected && selectedChoiceVisible && telephoneFilled //&& selectedBankCardVisible

        if (allStepsCompleted) {
            val city = "Екатеринбург"
            val street = streetEditText.text.toString()
            val house = houseEditText.text.toString()
            val floor = floorEditText.text.toString()
            val flat = flatEditText.text.toString()
            val doorphone = doorphoneEditText.text.toString()
            val comment = commentEditText.text.toString()

            // Поиск адреса доставки
            val deliveryAddressId = findOrCreateDeliveryAddress(city, street, house, floor, flat, doorphone, comment)

            // Создание новой записи в таблице Orders
            val orderId = createOrder(deliveryAddressId)

            // Перенос данных из таблицы CartClient в таблицу OrdersProduct
            moveCartItemsToOrdersProduct(orderId)

            // Обновление телефонного номера пользователя
            val telephone = telephoneEditText.text.toString()

            if(!isValidPhoneNumber(telephone)){
                Toast.makeText(this, "Введен не верный телефон", Toast.LENGTH_SHORT).show()
                return
            }

            // если верный номер
            updatePhoneNumberInDatabase(telephone)

            val totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextView)
            val finalPrice = totalPriceTextView.text

            val databaseHelper = DatabaseHelper(this)
            val database = databaseHelper.readableDatabase
            val productList = intent.getParcelableArrayListExtra<Order>("productList")

            // Запускам корутину для выполнения асинхронной операции отправки письма
            GlobalScope.launch(Dispatchers.IO) {
                sendEmailToClientWithProducts(database, productList as kotlin.collections.ArrayList<Order>)
            }

            // Создание intent для перехода на ProfFragment
            val intent = Intent(this, ProofActivity::class.java).apply {
                putExtra("finalPrice", finalPrice)
            }
            startActivity(intent)

            // Закрытие текущей активности (чтобы нельзя было вернуться на нее)
            finish()

            // Отображаем сообщение об успешной операции
            // Toast.makeText(this, "Заказ оформлен успешно", Toast.LENGTH_SHORT).show()
        } else {
            // Не все шаги выполнены

            // Отображаем сообщение о незавершенных шагах
            Toast.makeText(this, "Не все шаги выполнены", Toast.LENGTH_SHORT).show()
        }
    }

    // Проверка телефона на правильность написания
    fun isValidPhoneNumber(phone: String): Boolean {
        val regex = Regex("^(\\+7|8)?[- ]?\\d{10}\$")
        return regex.matches(phone)
    }

    //найти адрес доставки
    private fun findOrCreateDeliveryAddress(
        city: String?,
        street: String,
        house: String,
        floor: String,
        flat: String,
        doorphone: String,
        comment: String
    ): Long {
        val databaseHelper = DatabaseHelper(this)
        val db = databaseHelper.writableDatabase// получение экземпляра вашей базы данных
        val cursor = db.rawQuery("SELECT ID FROM DeliveryAdresses WHERE Street = ? AND House = ? AND Flat = ?",
            arrayOf(street, house, flat))

        return if (cursor != null && cursor.moveToFirst()) {
            // Адрес найден
            val addressId = cursor.getLong(cursor.getColumnIndexOrThrow("ID"))
            cursor.close()
            addressId
        } else {
            // Адрес не найден, создаем новую запись
            val values = ContentValues().apply {
                put("City", city?: "Екатеринбург")
                put("Street", street)
                put("House", house)
                put("Floor", floor)
                put("Flat", flat)
                put("Doorphone", doorphone)
                put("Comment", comment)
            }
            val newAddressId = db.insert("DeliveryAdresses", null, values)

            // Добавление записи в таблицу ClientsDeliveryAddresses
            val userId = UserManager.userId

            val clientsDeliveryValues = ContentValues().apply {
                put("Clients_id", userId)
                put("DeliveryAddresses_id", newAddressId)
            }

            db.insert("ClientsDeliveryAddresses", null, clientsDeliveryValues)

            newAddressId
        }
    }

    private fun createOrder(deliveryAddressId: Long): Long {
        val databaseHelper = DatabaseHelper(this)
        val db = databaseHelper.writableDatabase // получение экземпляра вашей базы данных
        val userId = UserManager.userId// получение ID пользователя из UserManager
        val calendar = Calendar.getInstance()
        val currentDateAndTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(calendar.time)

        val contactlessDeliveryButton: SwitchCompat = findViewById(R.id.contactlessDeliveryButton)
        var contactlessDelivery: Int = if (contactlessDeliveryButton.isChecked) 1 else 0

        val values = ContentValues().apply {
            put("Clients_id", userId)
            put("DeliveryAdresses_id", deliveryAddressId)
            put("Date", currentDateAndTime.substring(0, 10)) // Дата
            put("Time", currentDateAndTime.substring(11)) // Время
            put("ContactlessDelivery", contactlessDelivery)
        }

        return db.insert("Orders", null, values)
    }

    private fun moveCartItemsToOrdersProduct(orderId: Long) {
        val databaseHelper = DatabaseHelper(this)
        val db = databaseHelper.writableDatabase
        val userId = UserManager.userId?.toLongOrNull() ?: return // Преобразование к Long, или возврат из функции, если UserManager.userId null

        val cursor = db.rawQuery(
            "SELECT Products_id, Quantity FROM CartClient WHERE Clients_id = ?",
            arrayOf(userId.toString())
        )

        try {
            while (cursor.moveToNext()) {
                val productId = cursor.getLong(cursor.getColumnIndexOrThrow("Products_id"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))

                val values = ContentValues().apply {
                    put("Orders_id", orderId)
                    put("Products_id", productId)
                    put("Quantity", quantity)
                }

                db.insert("OrdersProduct", null, values)
            }
        } finally {
            cursor.close()
        }

        // Очистка записей из таблицы CartClient для указанного пользователя
        clearCartItemsForUser(userId)
    }

    private fun clearCartItemsForUser(userId: Long) {
        val databaseHelper = DatabaseHelper(this)
        val db = databaseHelper.writableDatabase

        db.delete("CartClient", "Clients_id = ?", arrayOf(userId.toString()))
    }

    private fun updatePhoneNumberInDatabase(telephone: String) {
        val databaseHelper = DatabaseHelper(this)
        val db = databaseHelper.writableDatabase

        val values = ContentValues().apply {
            put("Telephone", telephone)
        }

        val selection = "ID = ?"
        val selectionArgs = arrayOf(UserManager.userId.toString())

        db.update("Clients", values, selection, selectionArgs)
    }

    //заполнить телефон пользователя если он есть в базе данных
    private fun getInfoUserFromDatabase(database: SQLiteDatabase,
                                        telephone: EditText){
        val query = "select Telephone from Clients where Clients.ID = ${UserManager.userId}"
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val telephoneSQL = cursor.getString(cursor.getColumnIndexOrThrow("Telephone"))
            telephone.setText(telephoneSQL)
        }
    }

    // заполнить EditText'ы отвечающие за адрес
    override fun onItemClick(deliveryAdress: DeliveryAdress) {
        // Здесь вы получаете выбранный адрес доставки
        // и можете заполнить EditText на активити соответствующими данными
        Log.d("OrderRegistrationActivity", "onItemClick: $deliveryAdress")
        streetEditText.setText(deliveryAdress.street)
        houseEditText.setText(deliveryAdress.house)
        flatEditText.setText(deliveryAdress.flat)
        commentEditText.setText(deliveryAdress.comment)
    }

    //проверка на наличие адреса доставки
    private fun checkDeliveryAddressExists(database: SQLiteDatabase): Boolean {
        val query = "select COUNT(*) from ClientsDeliveryAddresses where Clients_id = ?"
        val cursor = database.rawQuery(query, arrayOf(UserManager.userId.toString()))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    private fun sendEmail(recipient: String, orders: List<Order>) {
        try {
            val street = streetEditText.text.toString()
            val house = houseEditText.text.toString()
            val flat = flatEditText.text.toString()
            val comment = commentEditText.text.toString()

            val adress = "$street, дом $house, квартира $flat"

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
            message.subject = "Ваш заказ"

            val orderList = StringBuilder()
            orderList.append("С вашего аккаунта был оформлен заказ.\nТовары в заказе:\n")
            for ((index, order) in orders.withIndex()) {
                val priceWithoutRubSign = order.Price.replace("₽", "")
                orderList.append("${order.Title} ${order.Quantity} шт. $priceWithoutRubSign руб.")
                if (index < orders.size - 1) {
                    orderList.append(",\n")
                } else {
                    orderList.append(".\n") // Замена последней запятой на точку
                }
            }

            orderList.append("\nАдрес доставки: $adress \nКомментарий к заказу $comment \n" +
                             "Дополнительную информацию можете посмотреть в приложении.\n" +
                             "Спасибо за покупку!")
            message.setText(orderList.toString())

            Transport.send(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Получить mail пользователя
    private fun getUserEmailFromDatabase(database: SQLiteDatabase): String? {
        val query = "SELECT Email FROM Clients WHERE ID = ${UserManager.userId}"
        val cursor = database.rawQuery(query, null)
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("Email"))
        } else {
            null
        }
    }

    //отправить письмо
    private fun sendEmailToClientWithProducts(database: SQLiteDatabase, orders: ArrayList<Order>) {
        val userEmail = getUserEmailFromDatabase(database)
        if (userEmail != null) {
            sendEmail(userEmail, orders)
        } else {
            // Обработка случая, когда email не найден в базе данных
        }
    }

    fun onExitButtonClick(view: View) {
        onBackPressed()
    }
}