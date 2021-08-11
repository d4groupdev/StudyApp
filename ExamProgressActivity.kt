package com.example.example.ui.nclex

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.example.MainAdapter
import com.example.example.R
import com.example.example.adapter.AdapterClick
import com.example.example.adapter.AdapterListener
import com.example.example.data.model.ExamProgress
import kotlinx.android.synthetic.main.activity_nclex_exam_progress.*
import kotlinx.android.synthetic.main.activity_video_library.recycler
import java.text.SimpleDateFormat
import java.util.*

class ExamProgressActivity : AppCompatActivity(), AdapterListener {

    private val listAdapter by lazy { MainAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.activity_nclex_exam_progress)

        val progress = intent.getSerializableExtra("examProgress") as? ExamProgress
        progress?.let {

            val dateParser = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale("us"))
            val parser = SimpleDateFormat("mm:ss", Locale("us"))
            val date = dateParser.parse(progress.date)
            date?.let {
                val mainHandler = Handler(Looper.getMainLooper())

                mainHandler.post(object : Runnable {
                    override fun run() {
                        if (progress.timeLimit == 0) {
                            val c = Calendar.getInstance()
                            c.time = date
                            c.add(Calendar.MINUTE, progress.timeLimit)

                            val d = Calendar.getInstance()
                            d.time = Date()
                            val difference = d.timeInMillis - c.timeInMillis
                            c.time = Date(difference)

                            time.text = parser.format(c.time)
                            mainHandler.postDelayed(this, 1000)
                        } else {
                            val c = Calendar.getInstance()
                            c.time = date
                            c.add(Calendar.MINUTE, progress.timeLimit)

                            val d = Calendar.getInstance()
                            d.time = Date()
                            val difference =
                                c.timeInMillis - d.timeInMillis + (1000 * 1000 * progress.timeLimit)
                            c.time = Date(difference)

                            time.text = parser.format(Date(difference))
                            mainHandler.postDelayed(this, 1000)
                        }
                    }
                })
            }

            completed.text = resources.getString(
                R.string.progress_results,
                progress.results.count().toString(),
                progress.questions.count()
            )
        }

        recycler.apply {
            layoutManager = GridLayoutManager(context, 4) // LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        overridePendingTransition(R.anim.back_in, R.anim.back_out)
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.back_in, R.anim.back_out)
    }

    fun backPressed() {
        onBackPressed()
    }

    override fun listen(click: AdapterClick?) {

    }
}