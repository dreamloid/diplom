package com.example.diplom

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.diplom.db.UserManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var userId: String ?= null

    private var isTextViewVisible = false

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Настройка кнопки для выхода из аккаунта
        view.findViewById<Button>(R.id.exitButton).setOnClickListener {
            // Отображение диалогового окна с подтверждением
            showLogoutConfirmationDialog()
        }

        val constraintLayout: ConstraintLayout = view.findViewById(R.id.infoConstraintLayout)
        val textView: TextView = view.findViewById(R.id.infoTextView)
        val imageView: ImageView = view.findViewById(R.id.arrowImageView)

        constraintLayout.setOnClickListener {
            if (isTextViewVisible) {
                textView.visibility = View.GONE
                imageView.setImageResource(R.drawable.ic_arrow2)
            } else {
                textView.visibility = View.VISIBLE
                imageView.setImageResource(R.drawable.ic_arrow5)
            }
            isTextViewVisible = !isTextViewVisible
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val BankCardFrameLayout = view.findViewById<ConstraintLayout>(R.id.BankCardFrameLayout)
        BankCardFrameLayout.setOnClickListener{
            val intent = Intent(activity, BankCardActivity::class.java)
            startActivity(intent)
        }

        val firstName = view.findViewById<TextView>(R.id.firstNameTextView)
        val telephone = view.findViewById<TextView>(R.id.telephoneTextView)
        val email = view.findViewById<TextView>(R.id.emailTextView)

        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase

        getInfoUser(database, firstName, telephone, email)

        val HistoryOrderFrameLayout = view.findViewById<ConstraintLayout>(R.id.histroyFrame)
        HistoryOrderFrameLayout.setOnClickListener{
            val historyOrderFragment = HistoryOrderFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_container, historyOrderFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val editProfileImageView = view.findViewById<ImageView>(R.id.editProfileImageView)
        editProfileImageView.setOnClickListener{
            val editProfileFragment = EditProfileFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_container, editProfileFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val telephoneButton = view.findViewById<Button>(R.id.telephoneButton)
        val phoneNumber = "89521423252"
        telephoneButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }

        val telegramButton = view.findViewById<Button>(R.id.telegramButton)
        val telegramUsername = "dreamloid"
        telegramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://t.me/$telegramUsername")
            startActivity(intent)
        }
    }

    fun getInfoUser(database: SQLiteDatabase,
                    firstNameTextView: TextView,
                    telephoneTextView: TextView,
                    emailTextView: TextView){
        userId = UserManager.userId

        val query = "select FirstName, Telephone, Email from Clients where ID = $userId"
        val cursor = database.rawQuery(query, null)
        if(cursor.moveToFirst()){
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow("FirstName"))
            val telephone = cursor.getString(cursor.getColumnIndexOrThrow("Telephone"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("Email"))

            firstNameTextView.text = firstName
            if (telephone == null) {
                telephoneTextView.text = "не указан"
            } else {
                telephoneTextView.text = telephone
            }
            emailTextView.text = email
        }
        cursor.close()
    }



    // Метод для отображения диалогового окна с подтверждением выхода
    private fun showLogoutConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exit_profile, null)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.positiveButton).setOnClickListener {
            // Действие при нажатии на кнопку "Да"
            dialog.dismiss()
            // Переход на экран аутентификации
            clearUserSession(requireContext())

            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        dialogView.findViewById<Button>(R.id.negativeButton).setOnClickListener {
            // Действие при нажатии на кнопку "Отмена"
            dialog.dismiss()
        }

        dialog.show()
    }

    fun clearUserSession(context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}