package com.example.diplom

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.diplom.db.UserManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CatalogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CatalogFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var imageViewPager: ViewPager2
    private lateinit var categoriesResView: RecyclerView

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
        val view = inflater.inflate(R.layout.fragment_catalog, container, false)
        categoriesResView = view.findViewById(R.id.categoriesRcView)

        var list = ArrayList<categoriesItem>()
        list.add(categoriesItem(R.drawable.ic_stocks, "Спецпредложения", 5))
        list.add(categoriesItem(R.drawable.ic_veterinarydrugs, "Ветеринарные препараты", 1))
        list.add(categoriesItem(R.drawable.ic_probioticsupplements, "Пробиотические добавки",2))
        list.add(categoriesItem(R.drawable.ic_aromaticadditives, "Ароматические добавки",3))
        list.add(categoriesItem(R.drawable.ic_vitamincomplexes, "Комплексы витаминов",4))

        categoriesResView.hasFixedSize()
        categoriesResView.layoutManager = LinearLayoutManager(requireContext())
        categoriesResView.adapter = AdapterForCategories(list, requireContext())

        val imageViewPager: ViewPager = view.findViewById(R.id.imageViewPager)
        val imageList = listOf(
            R.drawable.png_stock1,
            R.drawable.png_stock2
        )

        val imagePagerAdapter = AdapterImagePager(requireContext(), imageList)
        imageViewPager.adapter = imagePagerAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userIDTextView = view.findViewById<TextView>(R.id.userIDTextView)
        // Проверяем, что UserManager.userId не пустой и не null
        if (!UserManager.userId.isNullOrEmpty()) {
            // Заполняем TextView значением из UserManager.userId
            userIDTextView.text = UserManager.userId
        } else {
            // Если UserManager.userId пустой или null, выводим сообщение об этом
            userIDTextView.text = "UserID is empty or null"
        }

        val databaseHelper = DatabaseHelper(requireContext())
        val nearestDeliveryTime = databaseHelper.getNearestDeliveryTime()

        val formattedNearestDeliveryTime = if (nearestDeliveryTime.isNotEmpty()) {
            nearestDeliveryTime.replaceFirst(nearestDeliveryTime[0], nearestDeliveryTime[0].toLowerCase())
        } else {
            "неизвестное время"
        }


        //val formattedNearestDeliveryTime = nearestDeliveryTime.replaceFirst(nearestDeliveryTime[0], nearestDeliveryTime[0].toLowerCase())
        //val timeTextView = view.findViewById<TextView>(R.id.timeTextView)
        //timeTextView.text = "Доставим $formattedNearestDeliveryTime"

        val timeTextView = view.findViewById<TextView>(R.id.timeTextView)
        timeTextView.text = "Доставим $formattedNearestDeliveryTime"
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CatalogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CatalogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}