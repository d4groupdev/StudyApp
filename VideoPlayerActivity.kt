package com.example.example.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.example.R
import com.example.example.adapter.AdapterClick
import com.example.example.adapter.AdapterListener
import com.example.example.data.model.VideoDetails
import com.example.example.data.model.findVideo
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.android.synthetic.main.activity_video_view.*
import kotlinx.android.synthetic.main.video_library_list.*

class VideoPlayerActivity : AppCompatActivity(), SearchView.OnQueryTextListener, AdapterListener {

    lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)
        searchToolbar.setOnQueryTextListener(this)

        val videoDetails = intent.getSerializableExtra("videoDetails") as? VideoDetails
        videoDetails?.let {
            player = ExoPlayerFactory.newSimpleInstance(
                this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(), DefaultLoadControl()
            )
            simpleExoPlayerView.player = player

            player.playWhenReady = true

            if (videoDetails.video_url.contains(".mp4")) {
                val dataSourceFactory = DefaultHttpDataSourceFactory("ua")
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(videoDetails.video_url))
                player.prepare(mediaSource, true, false)
            } else {
                val mediaSource = buildMediaSource(Uri.parse(videoDetails.video_url))
                player.prepare(mediaSource, true, false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        player.stop()
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory("ua")
        return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val query = query.toString()
        val videos = findVideo(query)

        if (query == "") {
            recyclerSearch.visibility = View.INVISIBLE
        } else {
            recyclerSearch.visibility = View.VISIBLE
        }

        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val query = newText.toString()

        if (query == "") {
            recyclerSearch.visibility = View.INVISIBLE
        } else {
            recyclerSearch.visibility = View.VISIBLE
        }
        return true
    }

    override fun listen(click: AdapterClick?) {

    }
}