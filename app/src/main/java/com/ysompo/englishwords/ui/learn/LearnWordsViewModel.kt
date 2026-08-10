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
    val currentWordMastered: Boolean
)

class LearnWordsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val wordRepository = WordRepository(db)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<LearnState>()
    private val masteredIds = mutableSetOf<Int>()

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
        }
    }

    fun advanceToNextWord() {
        val current = state.value ?: return
        val nextIndex = current.currentIndex + 1
        if (nextIndex < current.words.size) {
            state.value = current.copy(currentIndex = nextIndex, currentWordMastered = false)
        }
    }

    fun isLastWordMastered(): Boolean {
        val current = state.value ?: return false
        return current.currentIndex == current.words.lastIndex && current.currentWordMastered
    }

    fun finishLearning(onDone: () -> Unit) {
        viewModelScope.launch {
            progressRepository.markWordsLearned(masteredIds.toList(), LocalDate.now())
            onDone()
        }
    }
}
