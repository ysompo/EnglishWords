package com.ysompo.englishwords.ui.progress

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.databinding.ActivityProgressBinding

class ProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressBinding
    private lateinit var viewModel: ProgressViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProgressViewModel::class.java]
        viewModel.state.observe(this) { state -> render(state) }
        viewModel.load()
    }

    private fun render(state: ProgressState) {
        binding.monthlyRewardBanner.visibility = if (state.currentMonthFullyStarred) View.VISIBLE else View.GONE

        binding.weeklyStarsText.text = state.weeklyStatuses.joinToString(" ") { if (it.starEarned) "⭐" else "☆" }
            .ifEmpty { "עוד לא הושלם אף שבוע" }

        binding.badgesContainer.removeAllViews()
        state.unlockedBadges.forEach { badge ->
            binding.badgesContainer.addView(TextView(this).apply {
                text = "🏅 ${badge.titleHe}"
                textSize = 16f
            })
        }
        state.lockedBadges.forEach { badge ->
            binding.badgesContainer.addView(TextView(this).apply {
                text = "🔒 ${badge.titleHe} (${badge.threshold} מילים)"
                textSize = 16f
                alpha = 0.5f
            })
        }
    }
}
