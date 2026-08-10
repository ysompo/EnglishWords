package com.ysompo.englishwords.data

import androidx.room.Entity

@Entity(tableName = "daily_completion", primaryKeys = ["date"])
data class DailyCompletionEntity(
    val date: String,
    val learningDone: Boolean,
    val quizDone: Boolean,
    val quizScore: Int
)
