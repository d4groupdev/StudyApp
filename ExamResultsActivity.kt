package com.example.example.ui.nclex

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.example.MainActivity
import com.example.example.MainAdapter
import com.example.example.R
import com.example.example.adapter.AdapterClick
import com.example.example.adapter.AdapterListener
import com.example.example.data.model.QuestionResult
import kotlinx.android.synthetic.main.activity_nclex_exam_results.*
import kotlinx.android.synthetic.main.activity_video_library.recycler
import java.io.ByteArrayOutputStream

class ExamResultsActivity : AppCompatActivity(), AdapterListener {


    private val listAdapter by lazy { MainAdapter(this) }

    var results = arrayListOf<QuestionResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

//        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_nclex_exam_results)

        val results = intent.getSerializableExtra("results") as? ArrayList<QuestionResult>
        results?.let {
            this.results = results

            recycler.apply {
                layoutManager = GridLayoutManager(context, 1)
                adapter = listAdapter
            }
                  }

        moreInfo.setOnClickListener {
            val image = takeScreenshotOfView(layout, layout.measuredHeight, layout.measuredWidth)
            val intent = Intent(this, ShareResultActivity::class.java)
            val bytes = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 50, bytes)
            intent.putExtra("image", bytes.toByteArray())
            this.startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    override fun onBackPressed() {

        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        finish()
    }

    fun backPressed() {
        onBackPressed()
    }

    override fun listen(click: AdapterClick?) {

    }

    private fun takeScreenshotOfView(view: View, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }
}