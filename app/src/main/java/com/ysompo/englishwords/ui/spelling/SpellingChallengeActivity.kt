package com.ysompo.englishwords.ui.spelling

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivitySpellingChallengeBinding
import com.ysompo.englishwords.logic.Encouragements
import com.ysompo.englishwords.logic.LetterState
import com.ysompo.englishwords.speech.TtsHelper
import com.ysompo.englishwords.ui.quiz.QuizActivity

class SpellingChallengeActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TARGET_WORD = "target_word"
    }

    private lateinit var binding: ActivitySpellingChallengeBinding
    private lateinit var viewModel: SpellingChallengeViewModel
    private lateinit var ttsHelper: TtsHelper
    private var targetWord: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpellingChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val word = intent.getStringExtra(EXTRA_TARGET_WORD)
        if (word.isNullOrEmpty()) {
            goToQuiz()
            return
        }
        targetWord = word

        ttsHelper = TtsHelper(this)
        viewModel = ViewModelProvider(this)[SpellingChallengeViewModel::class.java]
        viewModel.state.observe(this) { state -> render(state) }

        binding.guessInput.filters = arrayOf(InputFilter.LengthFilter(word.length))

        binding.listenButton.setOnClickListener { ttsHelper.speak(word) }
        binding.guessButton.setOnClickListener {
            val guess = binding.guessInput.text.toString()
            if (guess.length == word.length) {
                viewModel.submitGuess(guess)
                binding.guessInput.text?.clear()
            }
        }
        binding.continueButton.setOnClickListener { goToQuiz() }

        if (viewModel.state.value == null) {
            viewModel.start(word)
        }
        ttsHelper.speak(word)
    }

    private fun render(state: SpellingChallengeState) {
        binding.attemptsText.text = "ניסיונות שנשארו: ${state.attemptsRemaining}"

        binding.guessesContainer.removeAllViews()
        state.guesses.forEach { result -> binding.guessesContainer.addView(buildGuessRow(result)) }

        binding.guessInput.isEnabled = !state.gameOver
        binding.guessButton.isEnabled = !state.gameOver
        binding.continueButton.visibility = if (state.gameOver) View.VISIBLE else View.GONE

        if (state.gameOver) {
            binding.resultText.visibility = View.VISIBLE
            binding.resultText.text = if (state.solved) {
                "${Encouragements.random()} פתרת את זה! 🎉"
            } else {
                "בפעם הבאה! המילה הייתה: ${state.targetWord}"
            }
        } else {
            binding.resultText.visibility = View.GONE
        }
    }

    private fun buildGuessRow(result: GuessResult): LinearLayout {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val size = (40 * resources.displayMetrics.density).toInt()
        val margin = (2 * resources.displayMetrics.density).toInt()

        result.guess.forEachIndexed { index, letter ->
            val tile = TextView(this).apply {
                text = letter.uppercase()
                textSize = 22f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(
                    when (result.letterStates[index]) {
                        LetterState.CORRECT -> Color.parseColor("#7CB342")
                        LetterState.PRESENT -> Color.parseColor("#F5A623")
                        LetterState.ABSENT -> Color.parseColor("#9E9E9E")
                    }
                )
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = margin
                    marginEnd = margin
                }
            }
            row.addView(tile)
        }
        return row
    }

    private fun goToQuiz() {
        startActivity(Intent(this, QuizActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_QUIZ_MODE, QuizActivity.MODE_DAILY)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
    }
}
