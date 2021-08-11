package com.example.example.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.example.download.model.DownloadResult
import com.example.example.Prefs
import com.example.example.R
import com.example.example.adapter.*
import com.example.example.download.utils.Extensions
import com.example.example.download.utils.globalContext
import com.example.example.network.NetworkManager
import kotlinx.android.synthetic.main.fragment_study_guides.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class StudyGuidesFragment : Fragment(), StudyGuidesAdapter.Callback,
    StudyGuidesPageAdapter.Callback, SearchView.OnQueryTextListener {

    var api = NetworkManager.apiService
    lateinit var prefs: Prefs
    lateinit var adapterStudy: StudyGuidesAdapter
    var getStudyGuides: Call<StudyGuides>? = null
    lateinit var adapterStudyPage: StudyGuidesPageAdapter
    var libraryFile = mutableListOf<LibraryStudyGuides>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_study_guides, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = Prefs(requireContext())
        jellyToolbar.setOnQueryTextListener(this)
        getStudyGuides = api.getStudyGuidesAll(
            "Bearer ${prefs.getString("token")}",
            GetStudyGuidesAll(
                action = "mobile_study_guides_library_current_user"
            )
        )

        getStudyGuides!!.enqueue(object : Callback<StudyGuides> {
            override fun onResponse(
                call: Call<StudyGuides>,
                response: Response<StudyGuides>
            ) {
                if (response.isSuccessful)
                    if (response.body()!!.status) {
                        var library = mutableListOf<LibraryStudyGuides>()
                        for (item in response.body()!!.library) {
                            for (category in item.category_tree) {
                                if (category.guide_details != null)
                                    libraryFile.add(category)
                                else
                                    for (subcategory in category.category_tree)
                                        if (subcategory.guide_details != null)
                                            libraryFile.add(category)
                            }
                            if (item.guide_details == null)
                                library.add(item)
                            else libraryFile.add(item)
                        }
                        adapterStudy = StudyGuidesAdapter(
                            library,
                            requireContext(),
                            this@StudyGuidesFragment
                        )

                        recyclerFolder.apply {
                            layoutManager = GridLayoutManager(context, 1)
                            adapter = adapterStudy
                        }
                    }
            }

            override fun onFailure(call: Call<StudyGuides>, t: Throwable) {
                if (context != null) {
                    val toast = Toast.makeText(
                        context,
                        "Fail to generate Questions", Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
        })
        val callback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onItemClicked(data: String, categoryName: String) {
        val args = Bundle()
        args.putString("page", data)
        args.putString("categoryName", categoryName)
        findNavController().navigate(
            R.id.action_navigation_study_guides_to_navigation_study_guides_page,
            args
        )
    }

    override fun onPause() {
        super.onPause()
        if (getStudyGuides != null)
            getStudyGuides!!.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (getStudyGuides != null)
            getStudyGuides!!.cancel()
    }

    override fun onItemClickedFile(data: LibraryStudyGuides) {
        downloadWithFlow(data)
    }

    override fun onItemClickedFileDownloaded(data: String) {
        val args = Bundle()
        args.putString("url", data)
        findNavController().navigate(
            R.id.action_navigation_study_guides_to_navigation_pdf,
            args
        )
    }

    private val extension: Extensions = Extensions()

    private fun checkAndRequestPermissions(): Boolean {
        var i = 0
        val storageWrite = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val storageRead = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()

        if (storageWrite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (storageRead != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listPermissionsNeeded.toTypedArray(),
                i++
            )
            return false
        }
        return true
    }

    private fun downloadWithFlow(dummy: LibraryStudyGuides) {
        if (!checkAndRequestPermissions())
            return
        else
            CoroutineScope(Dispatchers.IO).launch {
                extension.downloadFile(
                    File(
                        globalContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        dummy.guide_details!!.guide_name.replace("\\s".toRegex(), "")
                            .toLowerCase() + ".pdf"
                    ), dummy.guide_details!!.guide_url
                ).collect {
                    withContext(Dispatchers.Main) {
                        when (it) {
                            is DownloadResult.Success -> {
                                adapterStudyPage.setDownloading(dummy, 100)
                            }
                            is DownloadResult.Error -> {
                                adapterStudyPage.setDownloading(dummy, 0)
                                Toast.makeText(
                                    requireContext(),
                                    "error saved",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is DownloadResult.Progress -> {
                                adapterStudyPage.setProgress(dummy, it.progress)
                            }
                        }
                    }
                }
            }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val text = query.toString()
        if (text == "") {
            recyclerSearch.visibility = View.GONE
        } else {
            val videos = search(text)
            recyclerSearch.visibility = View.VISIBLE
            adapterStudyPage = StudyGuidesPageAdapter(
                videos.toMutableList(),
                requireContext(),
                this
            )
            recyclerSearch.apply {
                layoutManager = GridLayoutManager(context, 1) // LinearLayoutManager(context)
                adapter = adapterStudy
            }
        }



        return true
    }

    private fun search(text: String): List<LibraryStudyGuides> {
        val list = mutableListOf<LibraryStudyGuides>()
        for (lib in libraryFile) {
            if (lib.guide_details != null)
                if (lib.guide_details.guide_name.contains(text, ignoreCase = true))
                    list.add(lib)
        }
        return list
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val query = newText.toString()

        if (query == "") {
            recyclerSearch.visibility = View.GONE
        } else {
            val videos = search(query)
            recyclerSearch.visibility = View.VISIBLE
            adapterStudyPage = StudyGuidesPageAdapter(
                videos.toMutableList(),
                requireContext(),
                this
            )
            recyclerSearch.apply {
                layoutManager = GridLayoutManager(context, 1)
                adapter = adapterStudyPage
            }
        }
        return true
    }
}