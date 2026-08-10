package com.ysompo.englishwords.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.state.observe(this) { state ->
            binding.wordsProgressBar.max = state.totalWords
            binding.wordsProgressBar.progress = state.learnedWords
            binding.wordsProgressText.text = "${state.learnedWords} מתוך ${state.totalWords} מילים"
            binding.streakText.text = "🔥 ${state.starredWeekStreak} שבועות ברצף"
            binding.nextBadgeText.text = if (state.nextBadgeTitle != null) {
                "עוד ${state.wordsUntilNextBadge} מילים ל: ${state.nextBadgeTitle}"
            } else {
                "פתחת את כל התגים!"
            }
            binding.startButton.text = if (state.todayComplete) "כבר סיימת היום, כל הכבוד!" else "התחל ללמוד היום"
            binding.weeklyQuizButton.visibility = if (state.weeklyQuizAvailable) View.VISIBLE else View.GONE
        }

        binding.startButton.setOnClickListener {
            // LearnWordsActivity is wired in Task 17
        }
        binding.weeklyQuizButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.quiz.QuizActivity::class.java).apply {
                putExtra(com.ysompo.englishwords.ui.quiz.QuizActivity.EXTRA_QUIZ_MODE, com.ysompo.englishwords.ui.quiz.QuizActivity.MODE_WEEKLY)
            })
        }
        binding.progressButton.setOnClickListener {
            // ProgressActivity is wired in Task 18
        }
        binding.settingsButton.setOnClickListener {
            // SettingsActivity is wired in Task 19
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }
}
