package com.ysompo.englishwords.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityQuizBinding
import com.ysompo.englishwords.ui.home.HomeActivity
import android.content.Intent

class QuizActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_QUIZ_MODE = "quiz_mode"
        const val MODE_DAILY = "daily"
        const val MODE_WEEKLY = "weekly"
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel
    private var mode: String = MODE_DAILY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra(EXTRA_QUIZ_MODE) ?: MODE_DAILY
        viewModel = ViewModelProvider(this)[QuizViewModel::class.java]
        viewModel.state.observe(this) { state -> render(state) }

        binding.doneButton.setOnClickListener {
            val onDone = {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            if (mode == MODE_DAILY) viewModel.finishDailyQuiz(onDone) else viewModel.finishWeeklyQuiz(onDone)
        }

        if (mode == MODE_DAILY) viewModel.loadDailyQuiz() else viewModel.loadWeeklyQuiz()
    }

    private fun render(state: QuizState) {
        binding.scoreText.text = "ניקוד: ${state.score}"

        if (state.questions.isEmpty()) {
            binding.questionText.visibility = View.GONE
            binding.optionsContainer.visibility = View.GONE
            binding.finishedText.visibility = View.VISIBLE
            binding.doneButton.visibility = View.VISIBLE
            binding.finishedText.text = "אין עדיין מילים לבחון"
            return
        }

        if (state.finished) {
            binding.questionText.visibility = View.GONE
            binding.optionsContainer.visibility = View.GONE
            binding.finishedText.visibility = View.VISIBLE
            binding.doneButton.visibility = View.VISIBLE
            binding.finishedText.text = "סיימת! ${state.score} מתוך ${state.questions.size} נכון"
            binding.confettiView.burst()
            return
        }

        val question = state.questions[state.currentIndex]
        binding.questionText.text = if (question.type == com.ysompo.englishwords.logic.QuestionType.SENTENCE_COMPLETION) {
            question.questionText
        } else {
            "מה התרגום של: ${question.questionText}?"
        }

        binding.optionsContainer.removeAllViews()
        question.options.forEach { option ->
            val button = Button(this).apply {
                text = option
                setOnClickListener { onOptionSelected(this, option, question.correctAnswer) }
            }
            binding.optionsContainer.addView(button)
        }
    }

    private fun onOptionSelected(button: Button, selected: String, correctAnswer: String) {
        button.setBackgroundColor(if (selected == correctAnswer) Color.parseColor("#7CB342") else Color.parseColor("#E57373"))
        for (i in 0 until binding.optionsContainer.childCount) {
            binding.optionsContainer.getChildAt(i).isEnabled = false
        }
        binding.root.postDelayed({ viewModel.submitAnswer(selected) }, 600)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.root.handler?.removeCallbacksAndMessages(null)
    }
}
