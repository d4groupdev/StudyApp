package com.example.example.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.example.R
import kotlinx.android.synthetic.main.study_guides_item.view.*
import kotlin.math.roundToInt


class StudyGuidesPageAdapter(
    var list: MutableList<LibraryStudyGuides>,
    val context: Context,
    val callback: Callback
) :
    RecyclerView.Adapter<StudyGuidesPageAdapter.FreeContentHolder>() {

    interface Callback {
        fun onItemClicked(data: String, categoryName: String)
        fun onItemClickedFile(data: LibraryStudyGuides)
        fun onItemClickedFileDownloaded(data: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FreeContentHolder {
        return FreeContentHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.study_guides_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: FreeContentHolder, position: Int) {
        if (list.isNullOrEmpty())
            return
        val item = list[position]
        holder.item.setOnClickListener {
            callback.onItemClicked(item.slug, item.category_name)
        }
        holder.namePackage.text = item.category_name
        holder.scoreGuide.text = item.category_guides.toString() + " guides"
        holder.sizeFolder.text =
            ((item.category_guides_size * 100.0).roundToInt() / 100.0).toString() + " MB"
    }

    fun setDownloading(file: LibraryStudyGuides, progress: Int) {
        getFile(file)?.download_progress = progress
        notifyItemChanged(list.indexOf(file), Bundle().apply { putInt("progress", progress) })
    }

    fun setProgress(file: LibraryStudyGuides, progress: Int) {
        getFile(file)?.download_progress = progress
        notifyItemChanged(list.indexOf(file), Bundle().apply { putInt("progress", progress) })
    }

    private fun getFile(file: LibraryStudyGuides) =
        list.find { file.guide_details?.guide_id == it.guide_details?.guide_id }

    class FreeContentHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namePackage = view.namePackage
        val scoreGuide = view.scoreGuide
        val sizeFolder = view.sizeFolder
        val progressBar = view.progressBar
        val item = view.item
    }
}