package com.ysompo.englishwords.ui.learn

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.R
import com.ysompo.englishwords.databinding.ActivityLearnWordsBinding
import com.ysompo.englishwords.speech.SpeechRecognitionHelper
import com.ysompo.englishwords.speech.TtsHelper

class LearnWordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLearnWordsBinding
    private lateinit var viewModel: LearnWordsViewModel
    private lateinit var ttsHelper: TtsHelper
    private lateinit var speechHelper: SpeechRecognitionHelper
    private val dotViews = mutableListOf<View>()
    // render() fires on every state change (a failed attempt, a successful match), not just when
    // moving to a new word - without this guard the word gets spoken again on each of those,
    // not just once when it first appears.
    private var lastSpokenWordIndex = -1

    private val requestAudioPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            android.widget.Toast.makeText(this, "צריך הרשאת מיקרופון כדי לתרגל הגייה", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsHelper = TtsHelper(this)
        speechHelper = SpeechRecognitionHelper(this)
        viewModel = ViewModelProvider(this)[LearnWordsViewModel::class.java]

        viewModel.state.observe(this) { state -> render(state) }

        binding.listenButton.setOnClickListener {
            viewModel.state.value?.let { state -> if (state.words.isNotEmpty()) ttsHelper.speak(state.words[state.currentIndex].word) }
        }
        binding.recordButton.setOnClickListener { requestAudioAndListen() }
        binding.nextButton.setOnClickListener {
            if (!viewModel.canProceedFromCurrentWord()) return@setOnClickListener
            if (viewModel.isLastWord()) {
                // Pick a word for the spelling mini-game before finishLearning() clears state -
                // any word from today's batch that's a comfortable 4-5 letter length for a
                // Wordle-style guess. If none qualify, skip straight to the quiz as before.
                val spellingWord = viewModel.state.value?.words?.firstOrNull { it.word.length in 4..5 }
                viewModel.finishLearning {
                    val nextScreen = if (spellingWord != null) {
                        Intent(this, com.ysompo.englishwords.ui.spelling.SpellingChallengeActivity::class.java).apply {
                            putExtra(com.ysompo.englishwords.ui.spelling.SpellingChallengeActivity.EXTRA_TARGET_WORD, spellingWord.word)
                        }
                    } else {
                        Intent(this, com.ysompo.englishwords.ui.quiz.QuizActivity::class.java).apply {
                            putExtra(com.ysompo.englishwords.ui.quiz.QuizActivity.EXTRA_QUIZ_MODE, com.ysompo.englishwords.ui.quiz.QuizActivity.MODE_DAILY)
                        }
                    }
                    startActivity(nextScreen)
                    finish()
                }
            } else {
                viewModel.advanceToNextWord()
            }
        }

        viewModel.load()
    }

    private fun render(state: LearnState) {
        if (state.words.isEmpty()) {
            binding.wordText.text = "כל הכבוד! למדת את כל המילים! 🎉"
            binding.translationText.visibility = View.GONE
            binding.successText.visibility = View.GONE
            binding.progressDotsContainer.visibility = View.GONE
            binding.listenButton.isEnabled = false
            binding.recordButton.isEnabled = false
            binding.nextButton.isEnabled = true
            binding.nextButton.text = "חזרה למסך הבית"
            binding.nextButton.setOnClickListener {
                startActivity(Intent(this, com.ysompo.englishwords.ui.home.HomeActivity::class.java))
                finish()
            }
            return
        }

        if (dotViews.size != state.words.size) {
            binding.progressDotsContainer.removeAllViews()
            dotViews.clear()
            val dotSize = (18 * resources.displayMetrics.density).toInt()
            val dotMargin = (6 * resources.displayMetrics.density).toInt()
            state.words.forEach { _ ->
                val dot = ImageView(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(dotSize, dotSize).apply {
                        marginStart = dotMargin
                        marginEnd = dotMargin
                    }
                    setBackgroundResource(R.drawable.bg_dot)
                }
                binding.progressDotsContainer.addView(dot)
                dotViews.add(dot)
            }
        }
        val completeColor = ContextCompat.getColor(this, R.color.gold_reward)
        val pendingColor = ContextCompat.getColor(this, R.color.locked_gray)
        dotViews.forEachIndexed { index, dot ->
            val isDone = index < state.currentIndex || (index == state.currentIndex && state.currentWordMastered)
            dot.backgroundTintList = ColorStateList.valueOf(if (isDone) completeColor else pendingColor)
        }

        val word = state.words[state.currentIndex]
        binding.wordText.text = word.word
        binding.translationText.text = word.translationHe

        val canSkip = !state.currentWordMastered && state.attemptsOnCurrentWord >= LearnWordsViewModel.MAX_ATTEMPTS_BEFORE_SKIP
        binding.nextButton.isEnabled = state.currentWordMastered || canSkip
        binding.nextButton.text = if (canSkip) "דלג למילה הבאה" else "הבא"
        binding.successText.visibility = if (state.currentWordMastered) View.VISIBLE else View.INVISIBLE

        if (state.currentWordMastered) {
            binding.successText.text = "${com.ysompo.englishwords.logic.Encouragements.random()} 🎉"
            playSuccessAnimation()
        }

        if (state.currentIndex != lastSpokenWordIndex) {
            lastSpokenWordIndex = state.currentIndex
            ttsHelper.speak(word.word)
        }
    }

    private fun playSuccessAnimation() {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1.2f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1.2f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(binding.successText, scaleX, scaleY).apply {
            duration = 400
            start()
        }
    }

    private fun requestAudioAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        speechHelper.startListening(
            onResult = { candidates -> viewModel.onRecognitionResult(candidates) },
            onError = { viewModel.onRecognitionFailed() }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
        speechHelper.destroy()
    }
}
