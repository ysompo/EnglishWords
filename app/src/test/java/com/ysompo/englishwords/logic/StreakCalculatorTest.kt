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
}
