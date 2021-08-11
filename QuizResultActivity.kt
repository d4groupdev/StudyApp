package com.example.example.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.example.MainActivity
import com.example.example.R
import com.example.example.adapter.*
import com.example.example.data.Prefs
import com.example.example.network.NetworkManager
import kotlinx.android.synthetic.main.activity_quiz_result.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuizResultActivity : AppCompatActivity() {
    var api = NetworkManager.apiService
    lateinit var prefs: Prefs
    lateinit var topicsResultAdapter: TopicsResultAdapter
    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        id = intent.getStringExtra("topic_id").toInt()
        setContentView(R.layout.activity_quiz_result)

        gauge2.value = 50

        btnClose.setOnClickListener {
            val intent = Intent(
                this,
                MainActivity::class.java
            )
            this.startActivity(intent)
        }

        btnRedo.setOnClickListener {
            val intent = Intent(
                this,
                QuizQuestionActivity::class.java
            )
            intent.putExtra("post_id", id.toString())
            this.startActivity(intent)
        }

        val generateQuiz = api.getQuizResult(
            "Bearer ${prefs.getString("token")}",
            QuizQuestion("get_quiz_results", quiz_id = id)
        )
        generateQuiz!!.enqueue(object : Callback<QuizResult> {
            override fun onResponse(
                call: Call<QuizResult>,
                response: Response<QuizResult>
            ) {
                if (response.isSuccessful) {
                    gauge2.value = response.body()!!.data.quizScore
                    textScore.text = response.body()!!.data.quizScore.toString()
                    videoTitle.text = "Results - ${response.body()!!.data.quizDate}"
                    topicsResultAdapter = TopicsResultAdapter(
                        response.body()!!.data.quizResults.toMutableList(),
                        this@QuizResultActivity,
                        false
                    )
                    topicsRecycler.apply {
                        layoutManager = GridLayoutManager(this@QuizResultActivity, 1)
                        adapter = topicsResultAdapter
                    }
                }
            }

            override fun onFailure(call: Call<QuizResult>, t: Throwable) {
                val toast = Toast.makeText(
                    this@QuizResultActivity,
                    "Fail to generate Questions", Toast.LENGTH_SHORT
                )
                toast.show()
            }
        })

        val userQuiz = api.getUserQuiz(
            "Bearer ${prefs.getString("token")}",
            QuizQuestion("mobile_get_user_quiz_performance", quiz_id = id)
        )

        userQuiz!!.enqueue(object : Callback<QuizUser> {
            override fun onResponse(
                call: Call<QuizUser>,
                response: Response<QuizUser>
            ) {
                if (response.isSuccessful) {
                    otherStudProgress.progress =
                        (response.body()!!.result.global_ratio * 100).toInt()
                    textOtherPercentStud.text =
                        (response.body()!!.result.global_ratio * 100).toInt().toString()
                }
            }

            override fun onFailure(call: Call<QuizUser>, t: Throwable) {
                val toast = Toast.makeText(
                    this@QuizResultActivity,
                    "Fail to generate Questions", Toast.LENGTH_SHORT
                )
                toast.show()
            }
        })

    }
}