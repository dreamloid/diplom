package com.example.diplom

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.diplom.db.UserManager
import com.example.diplom.db.insertData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "shopDB.db"
        private const val DATABASE_VERSION = 9
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Создание таблицы "UnitMeasurements"
        // В чем измеряется товар
        db.execSQL("""
        CREATE TABLE "UnitMeasurements" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "UnitMeasurement" TEXT NOT NULL,
            "Abbreviation" TEXT NOT NULL)""")
        // Создание таблицы "ProductTypes"
        // Типы товаров
        db.execSQL("""
        CREATE TABLE "ProductTypes" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "Title" TEXT NOT NULL)""")
        // Создание таблицы "Images"
        // Удалить мб
        db.execSQL("""
        CREATE TABLE "Images" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "Image" BLOB NOT NULL)""")
        // Создание таблицы "Products"
        // Товары
        db.execSQL("""
        CREATE TABLE "Products" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "Title" TEXT NOT NULL,
            "Description" TEXT,
            "Volume" REAL NOT NULL,
            "UnitMeasurements_id" INTEGER NOT NULL,
            "Price" REAL NOT NULL,
            "ProductTypes_id" INTEGER NOT NULL,
            "Images_id" INTEGER,
            "TitleEng" TEXT,
            FOREIGN KEY("Images_id") REFERENCES "Images"("ID"),
            FOREIGN KEY("ProductTypes_id") REFERENCES "ProductTypes"("ID"),
            FOREIGN KEY("UnitMeasurements_id") REFERENCES "UnitMeasurements"("ID"))""")
        // Создание таблицы "BankCards"
        // Банковские карты
        db.execSQL("""
        CREATE TABLE "BankCards" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "Number" TEXT NOT NULL,
            "Term" TEXT NOT NULL,
            "CVV" INTEGER NOT NULL,
            "BankSystem" text)""")
        // Создание таблицы "Clients"
        // Клиенты
        db.execSQL("""
        CREATE TABLE "Clients" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "LastName" TEXT NOT NULL,
            "FirstName" TEXT NOT NULL,
            "MiddleName" TEXT,
            "Email" TEXT,
            "Telephone" TEXT,
            "Password" TEXT NOT NULL,
            "VerificationCode" TEXT)""")
        insertTestClient(db)
        // Создание таблицы "DeliveryAdresses"
        // Адреса доставки
        db.execSQL("""
        CREATE TABLE "DeliveryAdresses" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "City" TEXT not null,
            "Street" TEXT NOT NULL,
            "House" INTEGER NOT NULL,
            "Flat" INTEGER NOT NULL,
            "Floor" integer,
            "Doorphone" text,
            "Comment" text)""")
        // Создание таблицы "ClientsDeliveryAddresses"
        // Адреса доставки определенного клиента
        db.execSQL("""
        CREATE TABLE "ClientsDeliveryAddresses" (
            "Clients_id" INTEGER NOT NULL,
            "DeliveryAddresses_id" INTEGER NOT NULL,
            FOREIGN KEY("Clients_id") REFERENCES "Clients"("ID"),
            FOREIGN KEY("DeliveryAddresses_id") REFERENCES "DeliveryAdresses"("ID"),
            PRIMARY KEY("Clients_id","DeliveryAddresses_id"))""")
        // Создание таблицы "ClientsBankCards"
        // Банковские карты определенного клиента
        db.execSQL("""
        CREATE TABLE "ClientsBankCards" (
            "Clients_id" INTEGER NOT NULL,
            "BankCards_id" INTEGER NOT NULL,
            FOREIGN KEY("Clients_id") REFERENCES "Clients"("ID"),
            FOREIGN KEY("BankCards_id") REFERENCES "BankCards"("ID"),
            PRIMARY KEY("Clients_id","BankCards_id"))""")
        // Создание таблицы "Orders"
        // Заказы
        db.execSQL("""
        CREATE TABLE "Orders" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            "Clients_id" INTEGER NOT NULL,
            "DeliveryAdresses_id" INTEGER NOT NULL,
            "Date" TEXT,
            "Time" TEXT,
            "ContactlessDelivery" integer,
            FOREIGN KEY("Clients_id") REFERENCES "Clients"("ID"),
            FOREIGN KEY("DeliveryAdresses_id") REFERENCES "DeliveryAdresses"("ID"))""")
        // Создание таблицы "OrdersProduct"
        // Товары в заказе
        db.execSQL("""
        CREATE TABLE "OrdersProduct" (
            "Orders_id" INTEGER NOT NULL,
            "Products_id" INTEGER NOT NULL,
            "Quantity" INTEGER NOT NULL,
            PRIMARY KEY("Orders_id","Products_id"),
            FOREIGN KEY("Orders_id") REFERENCES "Orders"("ID"),
            FOREIGN KEY("Products_id") REFERENCES "Products"("ID"))""")
        // Создание таблицы "Reviews"
        // Отзывы на товар
        db.execSQL("""
        CREATE TABLE "Reviews" (
            "ID" INTEGER PRIMARY KEY AUTOINCREMENT,
            "Products_id" INTEGER NOT NULL,
            "Client_id" INTEGER NOT NULL,
            "Score" INTEGER NOT NULL,
            "Dignities" TEXT,
            "Disadvantages" TEXT,
            "Comment" TEXT,
            FOREIGN KEY("Products_id") REFERENCES "Products"("ID"),
            FOREIGN KEY("Client_id") REFERENCES "Clients"("ID"))""")
        // Создание таблицы "CartClient"
        // Корзина клиента
        db.execSQL("""
        CREATE TABLE "CartClient" (
            "Clients_id" INTEGER NOT NULL,
            "Products_id" INTEGER NOT NULL,
            "Quantity" INTEGER NOT NULL,
            FOREIGN KEY("Products_id") REFERENCES "Products"("ID"),
            FOREIGN KEY("Clients_id") REFERENCES "Clients"("ID"),
            PRIMARY KEY("Clients_id","Products_id"))""")
        // Создание таблицы "LikesClient"
        // Понравившиеся продукты
        db.execSQL("""
        CREATE TABLE "LikesClient" (
            "Clients_id" INTEGER NOT NULL,
            "Products_id" INTEGER NOT NULL,
            FOREIGN KEY("Clients_id") REFERENCES "Clients"("ID"),
            FOREIGN KEY("Products_id") REFERENCES "Products"("ID"),
            PRIMARY KEY("Clients_id","Products_id"))""")

        db.execSQL("""
            create table "DeliveryDayAndTime" (
            "ID" integer primary key autoincrement,
            "Day" text not null,
            "Time" text not null)""")
        //
        db.execSQL("""
            create table "DeliveryDate" (
            "ID" integer primary key autoincrement,
            "Order_id" integer not null,
            "Day_id" integer not null,
            foreign key("Day_id") references "DeliveryDay"("ID"),
            foreign key("Order_id") references "Orders"("ID"))""")

        //Заполнение таблиц
        //val insertData = insertData(context)
        //insertData.fillData()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //db.execSQL("DROP DATABASE IF EXISTS $DATABASE_NAME")
        context.deleteDatabase(DATABASE_NAME)
        onCreate(db)
    }
    //Заполнение таблицы Client
    private fun insertTestClient(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put("LastName", "admin")
            put("FirstName", "admin")
            put("Email", "admin")
            put("Password", "admin")
            put("VerificationCode", "111111")
        }
        db.insert("Clients", null, values)
    }
    // Метод для авторизации пользователя
    fun authenticateUser(email: String, password: String): Boolean {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var isUserAuthenticated = false

        try {
            db = this.readableDatabase
            val selection = "Email = ? AND Password = ?"
            val selectionArgs = arrayOf(email, password)

            cursor = db.query("Clients", null, selection, selectionArgs, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex("ID")
                if (columnIndex != -1) {
                    // Если столбец "ID" найден в результирующем наборе
                    val userId = cursor.getInt(columnIndex)
                    UserManager.userId = userId.toString()
                    isUserAuthenticated = true
                } else {
                    // Столбец "ID" не найден
                    // Обработка ошибки или вывод сообщения об ошибке
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Закрываем курсор и базу данных после использования
            cursor?.close()
            db?.close()

            // Возвращаем значение false в случае, если не произошло исключение или не найден пользователь
            return isUserAuthenticated
        }
    }

    //проверка на существующий e-mail в базе
    fun isEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val selection = "Email = ?"
        val selectionArgs = arrayOf(email)

        val cursor = db.query("Clients", null, selection, selectionArgs, null, null, null)
        val isEmailExists = cursor.count > 0

        cursor.close()
        return isEmailExists
    }

    //Добавление клиента в базу
    fun saveClient(lastName:String, firstName:String, email:String, password:String, code: String) : Long {
        val writableDatabase = writableDatabase
        val contentValues = ContentValues().apply {
            put("LastName", lastName)
            put("FirstName", firstName)
            put("Email", email)
            put("Password", password)
            put("VerificationCode", code)
        }

        val insertedId = writableDatabase.insert("Clients", null, contentValues)
        writableDatabase.close()
        return insertedId
    }

    //Добавление отзыва в базу
    fun saveReview(productId: Int, score: Int, dignities: String, disadvantages: String, comment: String){
        val db = this.writableDatabase
        val userId = UserManager.userId
        if(userId != null){
            val contentValues = ContentValues().apply {
                put("Products_id", productId)
                put("Client_id", userId.toInt())
                put("Score", score)
                // Проверяем, не является ли значение null перед добавлением в ContentValues
                dignities?.let { put("Dignities", it) }
                disadvantages?.let { put("Disadvantages", it) }
                comment?.let { put("Comment", it) }
            }
            db.insert("Reviews", null, contentValues)
        }

        db.close()
    }
    //Добавление адреса доставки
    fun saveDeliveryAddress(city: String?, street: String, house: Int, flat: Int, floor: Int?, doorphone: String?, comment: String?) {
        val db = this.writableDatabase

        // Добавляем запись в таблицу DeliveryAdresses
        val deliveryAddressValues = ContentValues().apply {
            put("City", city ?: "Екатеринбург")
            put("Street", street)
            put("House", house)
            put("Flat", flat)
            put("Floor", floor)
            put("Doorphone", doorphone)
            put("Comment", comment)
        }
        val deliveryAddressId = db.insert("DeliveryAdresses", null, deliveryAddressValues)

        // Добавляем запись в таблицу ClientsDeliveryAddresses
        val userId = UserManager.userId // Получаем ID пользователя
        if (userId != null) {
            val clientsDeliveryValues = ContentValues().apply {
                put("Clients_id", userId.toInt()) // Преобразуем ID пользователя в Int
                put("DeliveryAddresses_id", deliveryAddressId) // Используем ID только что добавленного адреса доставки
            }
            db.insert("ClientsDeliveryAddresses", null, clientsDeliveryValues)
        }

        db.close()
    }

    //Увидеть всех клиентов
    @SuppressLint("Range")
    fun getAllClients(): List<Fivetuple<String, String, String, String, String>> {
        val clientsList = mutableListOf<Fivetuple<String, String, String, String, String>>()

        val readableDatabase = readableDatabase
        try {
            val cursor = readableDatabase.rawQuery("SELECT LastName, FirstName, Email, Password, VerificationCode FROM Clients", null)

            while (cursor.moveToNext()) {
                val lastName = cursor.getString(cursor.getColumnIndex("LastName")) ?: ""
                val firstName = cursor.getString(cursor.getColumnIndex("FirstName")) ?: ""
                val email = cursor.getString(cursor.getColumnIndex("Email")) ?: ""
                val password = cursor.getString(cursor.getColumnIndex("Password")) ?: ""
                val verificationCode = cursor.getString(cursor.getColumnIndex("VerificationCode")) ?: ""
                clientsList.add(Fivetuple(lastName, firstName, email, password, verificationCode))
            }
            cursor.close()
        } finally {
            // Закрываем базу данных после использования
            readableDatabase.close()
        }

        return clientsList
    }
    fun databaseExists(): Boolean {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return dbFile.exists()
    }

    fun isDatabaseEmpty(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT count(*) FROM Products", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count == 0
    }

    //получить данные по времени доставки
    fun getNearestDeliveryTime(): String {
        val currentTime = getCurrentTime()
        val todayQuery = "SELECT * FROM DeliveryDayAndTime WHERE Day = 'Сегодня' AND Time >= '$currentTime' LIMIT 1"
        val tomorrowQuery = "SELECT * FROM DeliveryDayAndTime WHERE Day = 'Завтра' LIMIT 1"
        val db = this.readableDatabase
        var cursor: Cursor = db.rawQuery(todayQuery, null)
        var nearestTime = ""

        if (cursor.moveToFirst()) {
            nearestTime = cursor.getString(cursor.getColumnIndexOrThrow("Day")) + " " + cursor.getString(cursor.getColumnIndexOrThrow("Time"))
        } else {
            cursor = db.rawQuery(tomorrowQuery, null)
            if (cursor.moveToFirst()) {
                nearestTime = cursor.getString(cursor.getColumnIndexOrThrow("Day")) + " " + cursor.getString(cursor.getColumnIndexOrThrow("Time"))
            }
        }
        cursor.close()
        db.close()
        return nearestTime
    }
    
    //функция чтобы узнать время системы
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return sdf.format(currentTime)
    }
}