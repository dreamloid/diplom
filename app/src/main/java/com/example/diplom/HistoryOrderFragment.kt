package com.example.diplom

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.adapter.History
import com.example.diplom.adapter.HistoryAdapter
import com.example.diplom.databinding.FragmentHistoryOrderBinding
import com.example.diplom.databinding.ItemHistoryLayoutBinding
import com.example.diplom.db.OrderManager
import com.example.diplom.db.UserManager
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryOrderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryOrderFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentHistoryOrderBinding
    private lateinit var historyAdapter: HistoryAdapter
    private var historyList: List<History> = emptyList()

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
        binding = FragmentHistoryOrderBinding.inflate(inflater, container, false)
        val view = binding.root

        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase

        this.historyList = getHistoryFromDatabase(database)

        historyAdapter = HistoryAdapter(requireContext(), historyList, object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(orderId: Int){
                // Сохраняем ID заказа в OrderManager
                OrderManager.orderId = orderId
                val intent = Intent(requireContext(), OrderActivity::class.java)
                startActivity(intent)
            } },
            object : HistoryAdapter.onButtonClickListener{
                override fun onButtonClick(orderId: Int) {
                    OrderManager.orderId = orderId
                    val intent = Intent(requireContext(), ChoiceReviewActivity::class.java)
                    startActivity(intent)
                }

        })

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.historyRecyclerView.adapter = historyAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arrowButton = view.findViewById<Button>(R.id.exitButton)

        arrowButton.setOnClickListener{
            Log.d("HistoryOrderFragment", "arrowButton clicked")
            val profileFragment = ProfileFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_container, profileFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

    private fun getHistoryFromDatabase(database: SQLiteDatabase): List<History> {
        val historyList = mutableListOf<History>()

        val query = "select Orders.ID, Date, Time from Orders, Clients where Orders.Clients_id = Clients.ID and Clients_id = ${UserManager.userId} order by Date desc, Time desc"
        val cursor = database.rawQuery(query, null)

        if(cursor.moveToFirst()){
            do{
                val orderId = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))

                val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("Date"))
                val timeStr = cursor.getString(cursor.getColumnIndexOrThrow("Time"))

                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse("$dateStr $timeStr")

                // Форматируем дату и время в требуемый формат "31 янв. в 23:59"
                val formattedDate = SimpleDateFormat("dd MMM 'в' HH:mm", Locale("ru")).format(date)

                // Добавляем данные в список historyList
                historyList.add(History(orderId, formattedDate))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return historyList
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryOrderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryOrderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}