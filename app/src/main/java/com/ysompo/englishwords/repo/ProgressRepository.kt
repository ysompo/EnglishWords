package com.ysompo.englishwords.repo

import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.LearningProgressEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity
import com.ysompo.englishwords.logic.WeekUtils
import java.time.LocalDate

class ProgressRepository(private val db: AppDatabase) {

    suspend fun learnedWordIds(): Set<Int> = db.learningProgressDao().getLearnedWordIds().toSet()

    suspend fun learnedWordCount(): Int = db.learningProgressDao().countLearned()

    suspend fun markWordsLearned(wordIds: List<Int>, date: LocalDate) {
        val dateText = WeekUtils.formatDate(date)
        wordIds.forEach { db.learningProgressDao().insert(LearningProgressEntity(it, dateText)) }
    }

    suspend fun recordDailyCompletion(date: LocalDate, learningDone: Boolean, quizDone: Boolean, quizScore: Int) {
        db.dailyCompletionDao().upsert(
            DailyCompletionEntity(WeekUtils.formatDate(date), learningDone, quizDone, quizScore)
        )
    }

    suspend fun completionForDate(date: LocalDate): DailyCompletionEntity? =
        db.dailyCompletionDao().getByDate(WeekUtils.formatDate(date))

    suspend fun completionsForWeek(weekStart: LocalDate): List<DailyCompletionEntity> {
        val weekEnd = weekStart.plusDays(6)
        return db.dailyCompletionDao().getBetween(WeekUtils.formatDate(weekStart), WeekUtils.formatDate(weekEnd))
    }

    suspend fun recordWeeklyStatus(weekStart: LocalDate, daysCompleted: Int, starEarned: Boolean, quizScore: Int?) {
        db.weeklyStatusDao().upsert(
            WeeklyStatusEntity(WeekUtils.formatDate(weekStart), daysCompleted, starEarned, quizScore)
        )
    }

    suspend fun allWeeklyStatuses(): List<WeeklyStatusEntity> = db.weeklyStatusDao().getAll()
}
