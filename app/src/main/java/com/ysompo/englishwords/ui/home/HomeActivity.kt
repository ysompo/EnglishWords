package com.ysompo.englishwords.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityHomeBinding
import com.ysompo.englishwords.notification.ReminderScheduler
import com.ysompo.englishwords.settings.ReminderSettings

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel

    private val requestNotificationPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* no-op either way, reminder still schedules; just won't show without permission */ }

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
            startActivity(Intent(this, com.ysompo.englishwords.ui.learn.LearnWordsActivity::class.java))
        }
        binding.weeklyQuizButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.quiz.QuizActivity::class.java).apply {
                putExtra(com.ysompo.englishwords.ui.quiz.QuizActivity.EXTRA_QUIZ_MODE, com.ysompo.englishwords.ui.quiz.QuizActivity.MODE_WEEKLY)
            })
        }
        binding.progressButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.progress.ProgressActivity::class.java))
        }
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, com.ysompo.englishwords.ui.settings.SettingsActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val settings = ReminderSettings(this)
        ReminderScheduler.schedule(this, settings.getReminderHour(), settings.getReminderMinute())
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }
}
