package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity
import org.junit.Test

class StreakCalculatorTest {

    private fun completion(date: String, done: Boolean) =
        DailyCompletionEntity(date, learningDone = done, quizDone = done, quizScore = if (done) 5 else 0)

    @Test
    fun `isDayComplete requires both learning and quiz done`() {
        assertThat(StreakCalculator.isDayComplete(null)).isFalse()
        assertThat(StreakCalculator.isDayComplete(DailyCompletionEntity("2026-08-10", learningDone = true, quizDone = false, quizScore = 0))).isFalse()
        assertThat(StreakCalculator.isDayComplete(DailyCompletionEntity("2026-08-10", learningDone = true, quizDone = true, quizScore = 5))).isTrue()
    }

    @Test
    fun `weekStarEarned is true at exactly 3 completed days out of 5`() {
        val threeDays = listOf(
            completion("2026-08-09", true),
            completion("2026-08-10", true),
            completion("2026-08-11", true),
            completion("2026-08-12", false),
            completion("2026-08-13", false)
        )
        assertThat(StreakCalculator.weekStarEarned(threeDays)).isTrue()
    }

    @Test
    fun `weekStarEarned is false at 2 completed days`() {
        val twoDays = listOf(
            completion("2026-08-09", true),
            completion("2026-08-10", true),
            completion("2026-08-11", false),
            completion("2026-08-12", false),
            completion("2026-08-13", false)
        )
        assertThat(StreakCalculator.weekStarEarned(twoDays)).isFalse()
    }

    @Test
    fun `isMonthFullyStarred requires every week in the list to have a star, and false when empty`() {
        assertThat(StreakCalculator.isMonthFullyStarred(emptyList())).isFalse()

        val allStarred = listOf(
            WeeklyStatusEntity("2026-08-02", 4, starEarned = true, weeklyQuizScore = 5),
            WeeklyStatusEntity("2026-08-09", 3, starEarned = true, weeklyQuizScore = 4)
        )
        assertThat(StreakCalculator.isMonthFullyStarred(allStarred)).isTrue()

        val oneMissing = allStarred + WeeklyStatusEntity("2026-08-16", 2, starEarned = false, weeklyQuizScore = 3)
        assertThat(StreakCalculator.isMonthFullyStarred(oneMissing)).isFalse()
    }

    @Test
    fun `consecutiveStarredWeeks is 0 for an empty list`() {
        assertThat(StreakCalculator.consecutiveStarredWeeks(emptyList())).isEqualTo(0)
    }

    @Test
    fun `consecutiveStarredWeeks counts all weeks when every week is starred and contiguous`() {
        val statuses = listOf(
            WeeklyStatusEntity("2026-07-26", 4, starEarned = true, weeklyQuizScore = 5),
            WeeklyStatusEntity("2026-08-02", 4, starEarned = true, weeklyQuizScore = 5),
            WeeklyStatusEntity("2026-08-09", 3, starEarned = true, weeklyQuizScore = 4)
        )
        assertThat(StreakCalculator.consecutiveStarredWeeks(statuses)).isEqualTo(3)
    }

    @Test
    fun `consecutiveStarredWeeks breaks the streak on a missing week, not just a non-starred one`() {
        // Week 2026-08-09 is starred, 2026-08-02 has no row at all (never played), 2026-07-26 is starred.
        // The gap at 2026-08-02 must break the streak, so the answer is 1, not 2.
        val statuses = listOf(
            WeeklyStatusEntity("2026-07-26", 4, starEarned = true, weeklyQuizScore = 5),
            WeeklyStatusEntity("2026-08-09", 3, starEarned = true, weeklyQuizScore = 4)
        )
        assertThat(StreakCalculator.consecutiveStarredWeeks(statuses)).isEqualTo(1)
    }

    @Test
    fun `consecutiveStarredWeeks is 0 when the most recent week is not starred, even if older weeks were`() {
        val statuses = listOf(
            WeeklyStatusEntity("2026-08-02", 4, starEarned = true, weeklyQuizScore = 5),
            WeeklyStatusEntity("2026-08-09", 1, starEarned = false, weeklyQuizScore = 2)
        )
        assertThat(StreakCalculator.consecutiveStarredWeeks(statuses)).isEqualTo(0)
    }
}
