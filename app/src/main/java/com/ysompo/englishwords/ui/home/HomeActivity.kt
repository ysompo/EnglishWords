package com.ysompo.englishwords.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ysompo.englishwords.BuildConfig
import com.ysompo.englishwords.R
import com.ysompo.englishwords.databinding.ActivityHomeBinding
import com.ysompo.englishwords.logic.LatestReleaseInfo
import com.ysompo.englishwords.logic.UpdateVersionComparator
import com.ysompo.englishwords.notification.ReminderScheduler
import com.ysompo.englishwords.settings.ReminderSettings
import com.ysompo.englishwords.update.UpdateChecker
import com.ysompo.englishwords.update.UpdateInstaller
import kotlinx.coroutines.launch

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

        checkForUpdate()
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    // Checked once per app launch (not on every onResume) - this is a personal APK with no
    // Play Store auto-update, so this is the only way it ever finds out about a newer version.
    private fun checkForUpdate() {
        lifecycleScope.launch {
            val release = UpdateChecker.fetchLatestRelease() ?: return@launch
            if (!UpdateVersionComparator.isNewer(release.versionTag, BuildConfig.VERSION_NAME)) return@launch
            if (release.apkDownloadUrl == null) return@launch
            if (!isFinishing && !isDestroyed) {
                showUpdateDialog(release)
            }
        }
    }

    private fun showUpdateDialog(release: LatestReleaseInfo) {
        AlertDialog.Builder(this)
            .setTitle(R.string.update_available_title)
            .setMessage(getString(R.string.update_available_message, release.versionTag))
            .setPositiveButton(R.string.update_now) { _, _ -> startUpdate(release) }
            .setNegativeButton(R.string.update_later, null)
            .show()
    }

    private fun startUpdate(release: LatestReleaseInfo) {
        val apkUrl = release.apkDownloadUrl ?: return

        if (!UpdateInstaller.canInstallPackages(this)) {
            Toast.makeText(this, R.string.update_grant_install_permission, Toast.LENGTH_LONG).show()
            UpdateInstaller.requestInstallPermission(this)
            return
        }

        Toast.makeText(this, R.string.update_downloading, Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            val apkFile = UpdateInstaller.downloadApk(this@HomeActivity, apkUrl)
            if (isFinishing || isDestroyed) return@launch
            if (apkFile == null) {
                Toast.makeText(this@HomeActivity, R.string.update_download_failed, Toast.LENGTH_LONG).show()
            } else {
                UpdateInstaller.installApk(this@HomeActivity, apkFile)
            }
        }
    }
}
