package com.ysompo.englishwords.ui.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.WordEntity
import com.ysompo.englishwords.logic.DailyLessonSelector
import com.ysompo.englishwords.logic.PronunciationMatcher
import com.ysompo.englishwords.repo.ProgressRepository
import com.ysompo.englishwords.repo.WordRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class LearnState(
    val words: List<WordEntity>,
    val currentIndex: Int,
    val currentWordMastered: Boolean,
    val attemptsOnCurrentWord: Int = 0
)

class LearnWordsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val wordRepository = WordRepository(db)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<LearnState>()
    private val masteredIds = mutableSetOf<Int>()

    companion object {
        // After this many attempts on a word without a match, let the child skip it rather than
        // get stuck - it simply won't be marked "learned" today, so it comes back another day.
        const val MAX_ATTEMPTS_BEFORE_SKIP = 4
    }

    fun load() {
        viewModelScope.launch {
            val allWords = wordRepository.allWordsOrdered()
            val learnedIds = progressRepository.learnedWordIds()
            val todaysWords = DailyLessonSelector.nextWordsToLearn(allWords, learnedIds)
            state.value = LearnState(todaysWords, currentIndex = 0, currentWordMastered = false)
        }
    }

    fun onRecognitionResult(candidates: List<String>) {
        val current = state.value ?: return
        val targetWord = current.words[current.currentIndex]
        val matched = candidates.any { PronunciationMatcher.isMatch(it, targetWord.word) }
        if (matched) {
            masteredIds.add(targetWord.id)
            state.value = current.copy(currentWordMastered = true)
        } else {
            registerFailedAttempt(current)
        }
    }

    fun onRecognitionFailed() {
        val current = state.value ?: return
        registerFailedAttempt(current)
    }

    private fun registerFailedAttempt(current: LearnState) {
        state.value = current.copy(attemptsOnCurrentWord = current.attemptsOnCurrentWord + 1)
    }

    fun canProceedFromCurrentWord(): Boolean {
        val current = state.value ?: return false
        return current.currentWordMastered || current.attemptsOnCurrentWord >= MAX_ATTEMPTS_BEFORE_SKIP
    }

    fun isLastWord(): Boolean {
        val current = state.value ?: return false
        return current.currentIndex == current.words.lastIndex
    }

    fun advanceToNextWord() {
        val current = state.value ?: return
        val nextIndex = current.currentIndex + 1
        if (nextIndex < current.words.size) {
            state.value = current.copy(currentIndex = nextIndex, currentWordMastered = false, attemptsOnCurrentWord = 0)
        }
    }

    fun finishLearning(onDone: () -> Unit) {
        viewModelScope.launch {
            progressRepository.markWordsLearned(masteredIds.toList(), LocalDate.now())
            onDone()
        }
    }
}
