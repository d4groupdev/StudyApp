package com.example.example.videoAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.example.R
import com.example.example.adapter.ListLibraryCategories
import kotlinx.android.synthetic.main.video_category_item.view.*

class CategoriesVideoAdapter(
    var list: MutableList<ListLibraryCategories>,
    val context: Context,
    val callback: Callback
) :
    RecyclerView.Adapter<CategoriesVideoAdapter.FreeContentHolder>(), VideosAdapter.Callback {

    lateinit var videosAdapter: VideosAdapter
    var collapseAll = false

    interface Callback {
        fun onItemClickedCategories(data: ListLibraryCategories, categoryName: String)
        fun onItemClickedMore(data: ListLibraryCategories)
    }

    fun setMovieListItems(movieList: MutableList<ListLibraryCategories>) {
        this.list.clear()
        this.list.addAll(movieList)
        notifyDataSetChanged()
    }

    fun collapseAll() {
        collapseAll = !collapseAll
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FreeContentHolder {
        return FreeContentHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.video_category_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FreeContentHolder, position: Int) {
        val videoList = mutableListOf<ListLibraryCategories>()
        val item = list[position]

        holder.title.text = item.category_name

        for(video in list[position].category_tree!!){
            if(video.video_details == null)
                videoList.addAll(video.category_tree!!)
            else
                videoList.add(video)
        }
        if (videoList.size > 3) {
            if(!collapseAll)
                holder.buttonMore.visibility = View.VISIBLE
            holder.buttonMore.setOnClickListener {
                onItemClicked(list[position])
            }
        }else{
            if(!collapseAll)
                holder.buttonMore.visibility = View.GONE
        }
        videosAdapter = VideosAdapter(
            videoList,
            holder.itemView.context,
            true,
            this
        )
        holder.recyclerVideo.apply {
            layoutManager = GridLayoutManager(context, 1) // LinearLayoutManager(context)
            adapter = videosAdapter
        }

        holder.moreRecycle.setOnClickListener {
            collapseAll = false
            holder.arrowMore.rotationX += 180f
            if(holder.recyclerVideo.visibility == View.VISIBLE) {
                holder.recyclerVideo.visibility = View.GONE
                holder.buttonMore.visibility = View.GONE
            }
            else {
                holder.recyclerVideo.visibility = View.VISIBLE
                if (list[position].category_tree!!.size > 3) {
                    holder.buttonMore.visibility = View.VISIBLE
                }
            }
        }

        holder.buttonMore.setOnClickListener {
            callback.onItemClickedMore(list[position])
        }

        if(collapseAll) {
            holder.arrowMore.rotationX = 180f
            holder.recyclerVideo.visibility = View.GONE
            holder.buttonMore.visibility = View.GONE
        }else{
            holder.arrowMore.rotationX = 0f
            holder.recyclerVideo.visibility = View.VISIBLE
            if (list[position].category_tree!!.size > 3) {
                holder.buttonMore.visibility = View.VISIBLE
            }
        }
    }

    class FreeContentHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerVideo = view.recyclerVideo
        val title = view.title
        val moreRecycle = view.moreRecycle
        val buttonMore = view.buttonMore
        val arrowMore = view.arrowMore
    }

    override fun onItemClicked(data: ListLibraryCategories) {
        for (it in list)
            for(nameVideo in it.category_tree!!) {
                if (data.video_details != null && nameVideo.video_details != null)
                    if (nameVideo.video_details.video_name == data.video_details.video_name)
                        callback.onItemClickedCategories(data, it.category_name ?: "")
                if(nameVideo.category_tree != null)
                    for (videos in nameVideo.category_tree){
                        if (data.video_details != null && videos.video_details != null)
                            if (videos.video_details.video_name == data.video_details.video_name)
                                callback.onItemClickedCategories(data, it.category_name ?: "")
                    }
            }
    }
}