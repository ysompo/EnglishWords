package com.ysompo.englishwords.ui.spelling

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.R
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
        // The English word must read/type left-to-right even though the rest of the app's UI
        // is Hebrew RTL - without this, letters would visually reverse (e.g. "GOOD" -> "D O O G").
        binding.guessInput.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.guessInput.textDirection = View.TEXT_DIRECTION_LTR

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
        // Force LTR here too, for the same reason as guessInput above - otherwise the tile
        // order visually reverses under the app's RTL layout direction.
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val size = (40 * resources.displayMetrics.density).toInt()
        val margin = (2 * resources.displayMetrics.density).toInt()

        result.guess.forEachIndexed { index, letter ->
            val tile = TextView(this).apply {
                text = letter.uppercase()
                textSize = 22f
                gravity = Gravity.CENTER
                typeface = ResourcesCompat.getFont(this@SpellingChallengeActivity, R.font.rubik)
                setTextColor(ContextCompat.getColor(this@SpellingChallengeActivity, R.color.text_on_color))
                setBackgroundResource(
                    when (result.letterStates[index]) {
                        LetterState.CORRECT -> R.drawable.bg_option_correct
                        LetterState.PRESENT -> R.drawable.bg_tile_present
                        LetterState.ABSENT -> R.drawable.bg_tile_absent
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
