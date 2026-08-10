package com.ysompo.englishwords.ui.learn

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityLearnWordsBinding
import com.ysompo.englishwords.speech.SpeechRecognitionHelper
import com.ysompo.englishwords.speech.TtsHelper

class LearnWordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLearnWordsBinding
    private lateinit var viewModel: LearnWordsViewModel
    private lateinit var ttsHelper: TtsHelper
    private lateinit var speechHelper: SpeechRecognitionHelper
    private val dotViews = mutableListOf<View>()

    private val requestAudioPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startListening() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsHelper = TtsHelper(this)
        speechHelper = SpeechRecognitionHelper(this)
        viewModel = ViewModelProvider(this)[LearnWordsViewModel::class.java]

        viewModel.state.observe(this) { state -> render(state) }

        binding.listenButton.setOnClickListener {
            viewModel.state.value?.let { ttsHelper.speak(it.words[it.currentIndex].word) }
        }
        binding.recordButton.setOnClickListener { requestAudioAndListen() }
        binding.nextButton.setOnClickListener {
            if (viewModel.isLastWordMastered()) {
                viewModel.finishLearning {
                    startActivity(Intent(this, com.ysompo.englishwords.ui.quiz.QuizActivity::class.java).apply {
                        putExtra(com.ysompo.englishwords.ui.quiz.QuizActivity.EXTRA_QUIZ_MODE, com.ysompo.englishwords.ui.quiz.QuizActivity.MODE_DAILY)
                    })
                    finish()
                }
            } else {
                viewModel.advanceToNextWord()
            }
        }

        viewModel.load()
    }

    private fun render(state: LearnState) {
        if (dotViews.size != state.words.size) {
            binding.progressDotsContainer.removeAllViews()
            dotViews.clear()
            state.words.forEach { _ ->
                val dot = ImageView(this).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(24, 24).apply { marginEnd = 12 }
                    setBackgroundColor(Color.LTGRAY)
                }
                binding.progressDotsContainer.addView(dot)
                dotViews.add(dot)
            }
        }
        dotViews.forEachIndexed { index, dot ->
            dot.setBackgroundColor(if (index < state.currentIndex || (index == state.currentIndex && state.currentWordMastered)) Color.parseColor("#F5A623") else Color.LTGRAY)
        }

        val word = state.words[state.currentIndex]
        binding.wordText.text = word.word
        binding.translationText.text = word.translationHe
        binding.nextButton.isEnabled = state.currentWordMastered
        binding.successText.visibility = if (state.currentWordMastered) View.VISIBLE else View.INVISIBLE

        if (state.currentWordMastered) {
            playSuccessAnimation()
        }

        ttsHelper.speak(word.word)
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
            onError = { /* allow the child to simply try again */ }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
        speechHelper.destroy()
    }
}
