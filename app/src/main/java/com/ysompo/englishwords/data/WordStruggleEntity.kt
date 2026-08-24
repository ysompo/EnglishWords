package com.ysompo.englishwords.data

import androidx.room.Entity

@Entity(tableName = "word_struggles", primaryKeys = ["wordId"])
data class WordStruggleEntity(
    val wordId: Int,
    val lastAttemptDate: String
)
