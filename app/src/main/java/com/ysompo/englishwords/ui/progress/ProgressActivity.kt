package com.ysompo.englishwords.ui.progress

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.ysompo.englishwords.R
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

        binding.levelsSummaryText.text = "${state.levelsCompleted} מתוך ${state.totalLevels} שלבים הושלמו"
        renderLevelPath(state)

        binding.badgesContainer.removeAllViews()
        state.unlockedBadges.forEach { badge -> binding.badgesContainer.addView(buildBadgeRow(badge.titleHe, locked = false)) }
        state.lockedBadges.forEach { badge ->
            binding.badgesContainer.addView(buildBadgeRow("${badge.titleHe} (${badge.threshold} מילים)", locked = true))
        }
    }

    private fun buildBadgeRow(text: String, locked: Boolean): TextView {
        val margin = (6 * resources.displayMetrics.density).toInt()
        val hPad = (14 * resources.displayMetrics.density).toInt()
        val vPad = (10 * resources.displayMetrics.density).toInt()
        return TextView(this).apply {
            this.text = if (locked) "🔒 $text" else "🏅 $text"
            textSize = 16f
            typeface = ResourcesCompat.getFont(this@ProgressActivity, R.font.rubik)
            setPadding(hPad, vPad, hPad, vPad)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { topMargin = margin }
            if (locked) {
                setBackgroundResource(R.drawable.bg_card)
                setTextColor(ContextCompat.getColor(this@ProgressActivity, R.color.text_charcoal_soft))
                alpha = 0.7f
            } else {
                setBackgroundResource(R.drawable.bg_pill_gold)
                setTextColor(ContextCompat.getColor(this@ProgressActivity, R.color.gold_reward_dark))
            }
        }
    }

    // Shows a short window of level "chips" around where the child currently is - a
    // Duolingo-style path rather than the whole (possibly 200-level) list at once.
    private fun renderLevelPath(state: ProgressState) {
        binding.levelPathContainer.removeAllViews()
        if (state.totalLevels == 0) return

        val windowStart = maxOf(1, state.currentLevel - 4)
        val windowEnd = minOf(state.totalLevels, state.currentLevel + 5)

        val completeColor = ContextCompat.getColor(this, R.color.success_green)
        val currentColor = ContextCompat.getColor(this, R.color.coral_primary)
        val lockedColor = ContextCompat.getColor(this, R.color.locked_gray)

        for (level in windowStart..windowEnd) {
            val completed = level <= state.levelsCompleted
            val isCurrent = level == state.currentLevel

            val chip = TextView(this).apply {
                text = if (completed) "✓" else level.toString()
                textSize = 16f
                typeface = ResourcesCompat.getFont(this@ProgressActivity, R.font.rubik)
                gravity = Gravity.CENTER
                setTextColor(
                    ContextCompat.getColor(
                        this@ProgressActivity,
                        if (completed || isCurrent) R.color.text_on_color else R.color.text_charcoal_soft
                    )
                )
                setBackgroundResource(R.drawable.bg_dot)
                backgroundTintList = ColorStateList.valueOf(
                    when {
                        completed -> completeColor
                        isCurrent -> currentColor
                        else -> lockedColor
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
