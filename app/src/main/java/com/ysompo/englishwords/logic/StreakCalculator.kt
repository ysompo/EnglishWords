package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.DailyCompletionEntity
import com.ysompo.englishwords.data.WeeklyStatusEntity

object StreakCalculator {
    const val MIN_DAYS_FOR_STAR = 3

    fun isDayComplete(completion: DailyCompletionEntity?): Boolean =
        completion != null && completion.learningDone && completion.quizDone

    fun weekStarEarned(completionsForWeek: List<DailyCompletionEntity>): Boolean {
        val completedDays = completionsForWeek.count { it.learningDone && it.quizDone }
        return completedDays >= MIN_DAYS_FOR_STAR
    }

    fun isMonthFullyStarred(weeklyStatusesInMonth: List<WeeklyStatusEntity>): Boolean {
        return weeklyStatusesInMonth.isNotEmpty() && weeklyStatusesInMonth.all { it.starEarned }
    }
}
