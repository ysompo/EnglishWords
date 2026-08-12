package com.ysompo.englishwords.ui.quiz

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.R
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
            val encouragement = com.ysompo.englishwords.logic.Encouragements.random()
            binding.finishedText.text = "$encouragement סיימת! ${state.score} מתוך ${state.questions.size} נכון"
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
        val optionMargin = (8 * resources.displayMetrics.density).toInt()
        question.options.forEach { option ->
            val button = Button(this).apply {
                text = option
                textSize = 17f
                isAllCaps = false
                typeface = ResourcesCompat.getFont(this@QuizActivity, R.font.rubik)
                setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.text_charcoal))
                setBackgroundResource(R.drawable.bg_option_default)
                setPadding(paddingLeft, (16 * resources.displayMetrics.density).toInt(), paddingRight, (16 * resources.displayMetrics.density).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = optionMargin }
                setOnClickListener { onOptionSelected(this, option, question.correctAnswer) }
            }
            binding.optionsContainer.addView(button)
        }
    }

    private fun onOptionSelected(button: Button, selected: String, correctAnswer: String) {
        val isCorrect = selected == correctAnswer
        button.setBackgroundResource(if (isCorrect) R.drawable.bg_option_correct else R.drawable.bg_option_incorrect)
        button.setTextColor(ContextCompat.getColor(this, R.color.text_on_color))
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
