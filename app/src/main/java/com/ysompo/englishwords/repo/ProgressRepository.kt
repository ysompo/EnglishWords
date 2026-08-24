package com.ysompo.englishwords.repo

import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.LearningProgressEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import java.time.LocalDate

class ProgressRepository(private val db: AppDatabase) {

    suspend fun learnedWordIds(): Set<Int> = db.learningProgressDao().getLearnedWordIds().toSet()

    // Most recently learned word first, so the "words I've learned" list on the progress screen
    // shows newest first.
    suspend fun learnedWordIdsByRecency(): List<Int> = db.learningProgressDao().getLearnedWordIdsByRecency()

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

    // The stored weekly_status table is only ever written when the child takes the optional
    // bonus "weekly quiz" (see QuizViewModel.finishWeeklyQuiz), which only becomes available
    // from Thursday of that week onward. A child who practices daily but never happens to open
    // the app during that window would otherwise show zero starred weeks despite real, qualifying
    // usage. This derives each week's star directly from the daily completions that are always
    // recorded by the daily lesson+quiz, so real usage is never lost - while still surfacing a
    // stored bonus-quiz score for weeks where one was taken.
    suspend fun effectiveWeeklyStatuses(): List<WeeklyStatusEntity> {
        val storedByWeek = db.weeklyStatusDao().getAll().associateBy { it.weekStartDate }
        return db.dailyCompletionDao().getAll()
            .groupBy { WeekUtils.formatDate(WeekUtils.weekStartFor(WeekUtils.parseDate(it.date))) }
            .map { (weekStartDate, completionsInWeek) ->
                WeeklyStatusEntity(
                    weekStartDate = weekStartDate,
                    daysCompleted = completionsInWeek.size,
                    starEarned = StreakCalculator.weekStarEarned(completionsInWeek),
                    weeklyQuizScore = storedByWeek[weekStartDate]?.weeklyQuizScore
                )
            }
            .sortedBy { it.weekStartDate }
    }
}
