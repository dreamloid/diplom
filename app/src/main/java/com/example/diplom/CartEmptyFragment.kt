package com.example.diplom

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.adapter.Similar
import com.example.diplom.adapter.SimilarAdapter
import com.example.diplom.databinding.FragmentCartEmptyBinding
import com.example.diplom.db.CategoryManager
import com.example.diplom.db.ProductManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CartEmptyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CartEmptyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentCartEmptyBinding
    private lateinit var similarAdapter: SimilarAdapter
    private var similarList: List<Similar> = emptyList()

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
        binding = FragmentCartEmptyBinding.inflate(inflater, container, false)

        val view = binding.root
        val databaseHelper = DatabaseHelper(requireContext())
        val database = databaseHelper.readableDatabase

        this.similarList = getProductsFromDatabase(database)
        similarAdapter = SimilarAdapter(requireContext(), similarList)
        binding.similarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        binding.similarRecyclerView.adapter = similarAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val catalogButton = view.findViewById<Button>(R.id.catalogButton)

        catalogButton.setOnClickListener {
            val catalogFragment = CatalogFragment()
            val transaction = requireActivity(). supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_container, catalogFragment)
            transaction.addToBackStack(null)
            transaction.commit()
            //requireActivity().finish()
        }

        similarAdapter.setOnProductClickListener(object : SimilarAdapter.OnProductClickListener {
            override fun onProductClick(productId: Int, categoryId: Int) {
                ProductManager.productId = productId
                CategoryManager.categoryId = categoryId
                val fragment = ProductFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        })
    }

    private fun getProductsFromDatabase(database: SQLiteDatabase): List<Similar> {
        val similarList = mutableListOf<Similar>()

        val query = "select Products.ID as ProductID, ProductTypes.ID as ProductTypeID, Products.Title, Volume, Price, TitleEng, Abbreviation " +
                    "from Products, UnitMeasurements, ProductTypes " +
                    "where UnitMeasurements.ID = Products.UnitMeasurements_id " +
                    "and ProductTypes.ID = Products.ProductTypes_id " +
                    "order by random() limit 9"
        val cursor = database.rawQuery(query, null)

        if(cursor.moveToFirst()){
            do{
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ProductID"))

                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("ProductTypeID"))

                val title = cursor.getString(cursor.getColumnIndexOrThrow("Title"))

                val volume = cursor.getDouble(cursor.getColumnIndexOrThrow("Volume"))
                val formattedVolume = String.format("%.0f", volume)

                val unitMeasurements = cursor.getString(cursor.getColumnIndexOrThrow("Abbreviation"))

                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
                val formattedPrice = String.format("%.0f", price)

                val titleEng = cursor.getString(cursor.getColumnIndexOrThrow("TitleEng"))

                val imageName = "png_$titleEng"
                val totalPrice = "$formattedPrice ₽"
                val totalVolume = "$formattedVolume $unitMeasurements"

                val similar = Similar(productId, categoryId, title, totalVolume, totalPrice, imageName, titleEng)
                similarList.add(similar)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return similarList
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CardEmptyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CartEmptyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}