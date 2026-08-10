package com.ysompo.englishwords.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.logic.BadgeCalculator
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import com.ysompo.englishwords.repo.ProgressRepository
import com.ysompo.englishwords.repo.WordRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeState(
    val totalWords: Int,
    val learnedWords: Int,
    val todayComplete: Boolean,
    val starredWeekStreak: Int,
    val nextBadgeTitle: String?,
    val wordsUntilNextBadge: Int,
    val weeklyQuizAvailable: Boolean
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val wordRepository = WordRepository(db)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<HomeState>()

    fun load() {
        viewModelScope.launch {
            wordRepository.ensureSeeded(getApplication())

            val totalWords = wordRepository.allWordsOrdered().size
            val learnedWords = progressRepository.learnedWordCount()
            val today = LocalDate.now()
            val todayCompletion = progressRepository.completionForDate(today)
            val todayComplete = StreakCalculator.isDayComplete(todayCompletion)

            val allStatuses = progressRepository.allWeeklyStatuses().sortedByDescending { it.weekStartDate }
            var streak = 0
            for (status in allStatuses) {
                if (status.starEarned) streak++ else break
            }

            val nextBadge = BadgeCalculator.nextBadge(learnedWords)

            val weekStart = WeekUtils.weekStartFor(today)
            val thisWeekThursday = weekStart.plusDays(4)
            val thisWeekAlreadyTaken = allStatuses.any { it.weekStartDate == WeekUtils.formatDate(weekStart) }
            val weeklyQuizAvailable = !today.isBefore(thisWeekThursday) && !thisWeekAlreadyTaken

            state.value = HomeState(
                totalWords = totalWords,
                learnedWords = learnedWords,
                todayComplete = todayComplete,
                starredWeekStreak = streak,
                nextBadgeTitle = nextBadge?.titleHe,
                wordsUntilNextBadge = nextBadge?.let { it.threshold - learnedWords } ?: 0,
                weeklyQuizAvailable = weeklyQuizAvailable
            )
        }
    }
}
