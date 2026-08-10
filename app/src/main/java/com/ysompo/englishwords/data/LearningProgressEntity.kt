package com.ysompo.englishwords.data

import androidx.room.Entity

@Entity(tableName = "learning_progress", primaryKeys = ["wordId"])
data class LearningProgressEntity(
    val wordId: Int,
    val learnedDate: String
)
