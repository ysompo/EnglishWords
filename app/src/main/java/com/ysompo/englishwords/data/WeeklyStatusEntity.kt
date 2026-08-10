package com.ysompo.englishwords.data

import androidx.room.Entity

@Entity(tableName = "weekly_status", primaryKeys = ["weekStartDate"])
data class WeeklyStatusEntity(
    val weekStartDate: String,
    val daysCompleted: Int,
    val starEarned: Boolean,
    val weeklyQuizScore: Int?
)
