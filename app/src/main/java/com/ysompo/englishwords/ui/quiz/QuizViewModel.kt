package com.ysompo.englishwords.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.logic.QuizQuestion
import com.ysompo.englishwords.logic.QuizQuestionGenerator
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import com.ysompo.englishwords.repo.ProgressRepository
import com.ysompo.englishwords.repo.WordRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuizState(
    val questions: List<QuizQuestion>,
    val currentIndex: Int,
    val score: Int,
    val lastAnswerCorrect: Boolean?,
    val finished: Boolean
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val wordRepository = WordRepository(db)
    private val progressRepository = ProgressRepository(db)

    val state = MutableLiveData<QuizState>()

    fun loadDailyQuiz() {
        viewModelScope.launch {
            val allWords = wordRepository.allWordsOrdered()
            val learnedIds = progressRepository.learnedWordIds()
            val today = LocalDate.now()
            // Words learned today are exactly the ones marked in LearnWordsActivity moments ago.
            val learnedTodayList = allWords.filter { it.id in learnedIds }.takeLast(5)
            val questions = QuizQuestionGenerator.dailyQuiz(learnedTodayList, allWords)
            state.value = QuizState(questions, 0, 0, null, false)
        }
    }

    fun loadWeeklyQuiz() {
        viewModelScope.launch {
            val allWords = wordRepository.allWordsOrdered()
            val learnedIds = progressRepository.learnedWordIds()
            val learnedWords = allWords.filter { it.id in learnedIds }
            val questions = QuizQuestionGenerator.weeklyQuiz(learnedWords, allWords, questionCount = 5)
            state.value = QuizState(questions, 0, 0, null, false)
        }
    }

    fun submitAnswer(selected: String) {
        val current = state.value ?: return
        val question = current.questions[current.currentIndex]
        val correct = selected == question.correctAnswer
        val newScore = current.score + if (correct) 1 else 0
        val nextIndex = current.currentIndex + 1
        val finished = nextIndex >= current.questions.size

        state.value = current.copy(
            currentIndex = if (finished) current.currentIndex else nextIndex,
            score = newScore,
            lastAnswerCorrect = correct,
            finished = finished
        )
    }

    fun finishDailyQuiz(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = state.value ?: return@launch
            val today = LocalDate.now()
            progressRepository.recordDailyCompletion(today, learningDone = true, quizDone = true, quizScore = current.score)
            onDone()
        }
    }

    fun finishWeeklyQuiz(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = state.value ?: return@launch
            val today = LocalDate.now()
            val weekStart = WeekUtils.weekStartFor(today)
            val completions = progressRepository.completionsForWeek(weekStart)
            val starEarned = StreakCalculator.weekStarEarned(completions)
            progressRepository.recordWeeklyStatus(weekStart, completions.size, starEarned, current.score)
            onDone()
        }
    }
}
