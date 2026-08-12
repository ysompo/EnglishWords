package com.ysompo.englishwords.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
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

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun render(state: ProgressState) {
        binding.monthlyRewardBanner.visibility = if (state.currentMonthFullyStarred) View.VISIBLE else View.GONE

        binding.weeklyStarsText.text = state.weeklyStatuses.joinToString(" ") { if (it.starEarned) "⭐" else "☆" }
            .ifEmpty { "עוד לא הושלם אף שבוע" }

        binding.levelsSummaryText.text = "${state.levelsCompleted} מתוך ${state.totalLevels} רמות הושלמו"
        renderLevelPath(state)

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

    // Shows a short window of level "chips" around where the child currently is - a
    // Duolingo-style path rather than the whole (possibly 200-level) list at once.
    private fun renderLevelPath(state: ProgressState) {
        binding.levelPathContainer.removeAllViews()
        if (state.totalLevels == 0) return

        val windowStart = maxOf(1, state.currentLevel - 4)
        val windowEnd = minOf(state.totalLevels, state.currentLevel + 5)

        for (level in windowStart..windowEnd) {
            val completed = level <= state.levelsCompleted
            val isCurrent = level == state.currentLevel

            val chip = TextView(this).apply {
                text = if (completed) "✓" else level.toString()
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(if (completed || isCurrent) Color.WHITE else Color.DKGRAY)
                setBackgroundColor(
                    when {
                        completed -> Color.parseColor("#7CB342")
                        isCurrent -> Color.parseColor("#4A90D9")
                        else -> Color.parseColor("#E0E0E0")
                    }
                )
            }
            val size = (48 * resources.displayMetrics.density).toInt()
            val margin = (6 * resources.displayMetrics.density).toInt()
            chip.layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginStart = margin
                marginEnd = margin
            }
            binding.levelPathContainer.addView(chip)
        }
    }
}
