package com.example.example.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.example.example.Prefs
import com.example.example.R
import com.example.example.adapter.*
import com.example.example.network.NetworkManager
import kotlinx.android.synthetic.main.fragment_all_library.*
import kotlinx.android.synthetic.main.video_library_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AllLibraryFragment : Fragment(), LibrarySubCategoriesAdapter.CallBack {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_all_library, container, false)
    }

    lateinit var librarySubCategoriesAdapter: LibrarySubCategoriesAdapter
    lateinit var prefs: Prefs
    var api = NetworkManager.apiService
    var getSubcategory: Call<LibraryCategories>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val categoryName = arguments!!.getString("category_name")

        prefs = Prefs(requireContext())
        getSubcategory = api.getVideoSubcategory(
            "Bearer ${prefs.getString("token")}",
            GetVideoSubcategory(
                action = "mobile_video_library",
                category = categoryName!!
            )
        )
        getSubcategory!!.enqueue(object : Callback<LibraryCategories> {
            override fun onResponse(
                call: Call<LibraryCategories>,
                response: Response<LibraryCategories>
            ) {
                name_list.text = response.body()!!.library[0].category_name
                librarySubCategoriesAdapter = LibrarySubCategoriesAdapter(response.body()!!.library[0].category_tree!!.toMutableList(),
                    requireContext(), this@AllLibraryFragment)
                recycler.apply {
                    layoutManager = GridLayoutManager(context, calculateNoOfColumns())
                    adapter = librarySubCategoriesAdapter
                }
            }
            override fun onFailure(call: Call<LibraryCategories>, t: Throwable) {
                Log.d("testing", "t $t")
            }
        })

        back_button.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun calculateNoOfColumns(
    ): Int {
        val displayMetrics: DisplayMetrics = this.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / 130 + 0.5).toInt()
    }

    override fun onPause() {
        if (getSubcategory != null)
            getSubcategory!!.cancel()
        super.onPause()
    }


    override fun onClickItem(name: ListLibraryCategories) {
        val intent = Intent(requireContext(), VideoLibraryActivity::class.java)
        val gson = Gson()
        intent.putExtra("library", gson.toJson(name))
        startActivity(intent)
    }
}