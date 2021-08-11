package com.example.example.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.example.R
import com.example.example.adapter.*
import com.example.example.data.Prefs
import com.example.example.data.model.InsightsPagedViewModel
import com.example.example.network.NetworkManager
import com.example.example.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_quiz_result.*
import kotlinx.android.synthetic.main.fragment_quiz_insights.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.round


class InsightsFragment : Fragment(), ItemInsightAdapter.CallBack {
    var api = NetworkManager.apiService
    lateinit var prefs: Prefs
    var contextThis: Activity? = null
    var adapterInsights: ItemInsightAdapter? = null
    var insights: Call<QuizInsightsRequest>? = null
    var list = mutableListOf<QuizUserInsights>()
    var listAll = mutableListOf<QuizUserInsights>()
    private var dialog: Dialog? = null
    private lateinit var viewModel: InsightsPagedViewModel
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)

        return inflater.inflate(R.layout.fragment_quiz_insights, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = Prefs(requireContext())
        contextThis = requireActivity()
        back_button.setOnClickListener {
            findNavController().popBackStack()
        }


        insights = api.getQuizInsights(
            "Bearer ${prefs.getString("token")}",
            QuizInsights("mobile_get_quiz_insights")
        )
        insights!!.enqueue(object : Callback<QuizInsightsRequest> {
            override fun onResponse(
                call: Call<QuizInsightsRequest>,
                response: Response<QuizInsightsRequest>
            ) {
                loadingPanel.visibility = View.GONE
                nestedInsight.visibility = View.VISIBLE
                if (response.code() == 403) {
                    val intent = Intent(context, LoginActivity::class.java)
                    prefs.putString("token", "null")
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    requireContext().startActivity(intent)
                }
                if (response.isSuccessful) {
                    val responce = response.body()!!
                    if (!responce.user_quizzes.isNullOrEmpty()) {
                        viewModel =
                            ViewModelProvider.AndroidViewModelFactory(activity!!.application)
                                .create(InsightsPagedViewModel::class.java)
                        adapterInsights = ItemInsightAdapter(
                            requireContext(),
                            prefs.getString(
                                "token"
                            ),
                            this@InsightsFragment,
                            false
                        )
                        viewModel.getPagedLiveDataQuizInsights(responce.user_quizzes)
                            .observe(viewLifecycleOwner, Observer {
                                adapterInsights!!.submitList(it)
                            })
                        val manager = LinearLayoutManager(requireContext())
                        quizInsightsRecycle.apply {
                            layoutManager = manager
                            adapter = adapterInsights
                            isNestedScrollingEnabled = true
                        }
                        testNumber.text = responce.user_quizzes.size.toString()
                        for (item in responce.user_quizzes) {
                            listAll.add(item)
                        }
                        coroutineScope.launch {
                            var correct = 0
                            var inCorrect = 0
                            for (item in responce.user_quizzes) {
                                correct += item.performance.correct_answers
                                inCorrect += item.performance.incorrect_answers

                            }
                            var scoreBig = "0"
                            var scoreSmall = ".0%"
                            if ((correct + inCorrect) != 0) {
                                val scoreBeforeRound =
                                    correct.toDouble() / (inCorrect.toDouble() + correct.toDouble()) * 100
                                val score = round((scoreBeforeRound * 10)) / 10.0
                                val scoreArray = score.toString().split(".")
                                scoreBig = scoreArray[0]
                                scoreSmall = when (scoreArray.size > 1) {
                                    true -> ".${scoreArray[1]}%"
                                    false -> ".0%"
                                }
                                scoreTest.text = scoreBig
                                otherScoreTest.text = scoreSmall
                            }
                            withContext(Dispatchers.Main) {
                                scoreIncorrect.text = inCorrect.toString()
                                correctText.text = correct.toString()
                                scoreTest.text = scoreBig
                                otherScoreTest.text = scoreSmall
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<QuizInsightsRequest>, t: Throwable) {
                val toast = Toast.makeText(
                    contextThis,
                    "Fail to generate Insights", Toast.LENGTH_SHORT
                )
                toast.show()
                loadingPanel.visibility = View.GONE
                nestedInsight.visibility = View.VISIBLE
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        if (insights != null)
            insights!!.cancel()
    }

    override fun onClick(id: String) {
        val intent = Intent(
            requireContext(),
            QuizResultActivity::class.java
        )
        intent.putExtra("topic_id", id)
        requireContext().startActivity(intent)
    }
}