package com.example.diplom

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.diplom.db.UserManager
import com.example.diplom.db.UserManager.Companion.userId

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firstNameEditText = view.findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = view.findViewById<EditText>(R.id.lastNameEditText)
        val middleNameEditText = view.findViewById<EditText>(R.id.middleNameEditText)
        val telephoneEditText = view.findViewById<EditText>(R.id.telephoneEditText)
        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)

        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase

        getInfoUser(database, firstNameEditText, lastNameEditText, middleNameEditText, telephoneEditText, emailEditText)

        val exitButton = view.findViewById<Button>(R.id.exitButton)

        exitButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val saveButton = view.findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val telephone = telephoneEditText.text.toString()
            val email = emailEditText.text.toString()

            if (telephone.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните номер телефона", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните email", Toast.LENGTH_SHORT).show()
            } else {
                updateClientInfo(database, firstNameEditText, lastNameEditText, middleNameEditText, telephoneEditText, emailEditText, UserManager.userId?.toInt() ?: 0)
            }
        }
    }

    fun getInfoUser(
        database: SQLiteDatabase,
        firstNameEditText: EditText,
        lastNameEditText: EditText,
        middleNameEditText: EditText,
        telephoneEditText: EditText,
        emailEditText: EditText){
        val query =
            "select FirstName, LastName, MiddleName, Email, Telephone from Clients where ID = ${UserManager.userId}"
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow("FirstName"))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow("LastName"))
            val middleName = cursor.getString(cursor.getColumnIndexOrThrow("MiddleName"))
            val telephone = cursor.getString(cursor.getColumnIndexOrThrow("Telephone"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("Email"))

            firstNameEditText.text = Editable.Factory.getInstance().newEditable(firstName)
            lastNameEditText.text = Editable.Factory.getInstance().newEditable(lastName)

            if (middleName == null) {
                middleNameEditText.text = Editable.Factory.getInstance().newEditable("")
            } else {
                middleNameEditText.text = Editable.Factory.getInstance().newEditable(middleName)
            }

            if (telephone == null) {
                telephoneEditText.text = Editable.Factory.getInstance().newEditable("")
            } else {
                telephoneEditText.text = Editable.Factory.getInstance().newEditable(telephone)
            }

            emailEditText.text = Editable.Factory.getInstance().newEditable(email)
        }
        cursor.close()
    }

    fun updateClientInfo(
        database: SQLiteDatabase,
        firstNameEditText: EditText,
        lastNameEditText: EditText,
        middleNameEditText: EditText,
        telephoneEditText: EditText,
        emailEditText: EditText,
        userId: Int
    ) {
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val middleName = middleNameEditText.text.toString()
        val telephone = telephoneEditText.text.toString()
        val email = emailEditText.text.toString()

        if (!isValidEmail(email)) {
            Toast.makeText(emailEditText.context, "Неправильный формат email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidPhoneNumber(telephone)) {
            Toast.makeText(telephoneEditText.context, "Неправильный формат номера телефона", Toast.LENGTH_SHORT).show()
            return
        }


        // Проверка на наличие текста в полях firstNameEditText и lastNameEditText
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(firstNameEditText.context, "Имя и Фамилия не могут быть пустыми", Toast.LENGTH_SHORT).show()
            return
        }

        val contentValues = ContentValues().apply {
            put("FirstName", firstName)
            put("LastName", lastName)
            put("MiddleName", middleName)
            put("Email", email)
            put("Telephone", telephone)
        }

        val whereClause = "ID = ?"
        val whereArgs = arrayOf(userId.toString())

        database.update("Clients", contentValues, whereClause, whereArgs)
        onUpdateComplete()
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        val regex = Regex("^(\\+7|8)?[- ]?\\d{10}\$")
        return regex.matches(phone)
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun onUpdateComplete() {
        // Переход на другой фрагмент после успешного обновления данных
        val profileFragment = ProfileFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, profileFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}