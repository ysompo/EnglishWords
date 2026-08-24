package com.ysompo.englishwords.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.WeeklyStatusEntity
import com.ysompo.englishwords.data.WordEntity
import com.ysompo.englishwords.logic.Badge
import com.ysompo.englishwords.logic.BadgeCalculator
import com.ysompo.englishwords.logic.LevelProgressCalculator
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import com.ysompo.englishwords.repo.ProgressRepository
import com.ysompo.englishwords.repo.WordRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class LearnedWordRow(val word: WordEntity, val needsPractice: Boolean)

data class ProgressState(
    val weeklyStatuses: List<WeeklyStatusEntity>,
    val learnedWordCount: Int,
    val unlockedBadges: List<Badge>,
    val lockedBadges: List<Badge>,
    val currentMonthFullyStarred: Boolean,
    val levelsCompleted: Int,
    val currentLevel: Int,
    val totalLevels: Int,
    val wordsCompletedInCurrentLevel: Int,
    val learnedWords: List<LearnedWordRow>
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val progressRepository = ProgressRepository(db)
    private val wordRepository = WordRepository(db)

    val state = MutableLiveData<ProgressState>()

    fun load() {
        viewModelScope.launch {
            val allStatuses = progressRepository.effectiveWeeklyStatuses().sortedBy { it.weekStartDate }
            val learnedCount = progressRepository.learnedWordCount()
            val allWords = wordRepository.allWordsOrdered()
            val totalWordCount = allWords.size

            val wordsById = allWords.associateBy { it.id }
            val learnedWordsList = progressRepository.wordProgressEntries().mapNotNull { entry ->
                wordsById[entry.wordId]?.let { LearnedWordRow(it, needsPractice = !entry.mastered) }
            }

            val currentMonth = YearMonth.from(LocalDate.now())
            val statusesInCurrentMonth = allStatuses.filter {
                YearMonth.from(WeekUtils.parseDate(it.weekStartDate)) == currentMonth
            }
            val monthFullyStarred = StreakCalculator.isMonthFullyStarred(statusesInCurrentMonth)

            val unlocked = BadgeCalculator.unlockedBadges(learnedCount)
            val locked = BadgeCalculator.ALL_BADGES - unlocked.toSet()

            state.value = ProgressState(
                weeklyStatuses = allStatuses,
                learnedWordCount = learnedCount,
                unlockedBadges = unlocked,
                lockedBadges = locked,
                currentMonthFullyStarred = monthFullyStarred,
                levelsCompleted = LevelProgressCalculator.levelsCompleted(learnedCount),
                currentLevel = LevelProgressCalculator.currentLevelNumber(learnedCount),
                totalLevels = LevelProgressCalculator.totalLevels(totalWordCount),
                wordsCompletedInCurrentLevel = LevelProgressCalculator.wordsCompletedInCurrentLevel(learnedCount),
                learnedWords = learnedWordsList
            )
        }
    }
}
