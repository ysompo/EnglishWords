package com.ysompo.englishwords.repo

import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.LearningProgressEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity
import com.ysompo.englishwords.data.WordStruggleEntity
import com.ysompo.englishwords.logic.StreakCalculator
import com.ysompo.englishwords.logic.WeekUtils
import java.time.LocalDate

// One row per word the child has ever encountered in a way worth showing on the progress screen:
// mastered (learned = true), or attempted but not mastered - skipped after too many failed
// pronunciation attempts in the daily lesson, or answered wrong in a quiz (learned = false).
// `date` is whichever of those events is on record for this word, used to order the list.
data class WordProgressEntry(val wordId: Int, val date: String, val mastered: Boolean)

class ProgressRepository(private val db: AppDatabase) {

    suspend fun learnedWordIds(): Set<Int> = db.learningProgressDao().getLearnedWordIds().toSet()

    suspend fun learnedWordCount(): Int = db.learningProgressDao().countLearned()

    suspend fun markWordsLearned(wordIds: List<Int>, date: LocalDate) {
        val dateText = WeekUtils.formatDate(date)
        wordIds.forEach { db.learningProgressDao().insert(LearningProgressEntity(it, dateText)) }
    }

    // Records a word the child failed to translate/pronounce (skipped in the daily lesson after
    // too many attempts) or answered incorrectly in a quiz, so it still shows up - flagged as
    // needing more practice - in the "words I've learned" list rather than disappearing silently.
    suspend fun markWordsStruggled(wordIds: List<Int>, date: LocalDate) {
        val dateText = WeekUtils.formatDate(date)
        wordIds.forEach { db.wordStruggleDao().insert(WordStruggleEntity(it, dateText)) }
    }

    suspend fun wordProgressEntries(): List<WordProgressEntry> {
        val learned = db.learningProgressDao().getAll().associateBy { it.wordId }
        val struggled = db.wordStruggleDao().getAll().associateBy { it.wordId }
        return (learned.keys + struggled.keys).map { wordId ->
            val learnedEntry = learned[wordId]
            WordProgressEntry(
                wordId = wordId,
                date = learnedEntry?.learnedDate ?: struggled[wordId]!!.lastAttemptDate,
                mastered = learnedEntry != null
            )
        }.sortedByDescending { it.date }
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
