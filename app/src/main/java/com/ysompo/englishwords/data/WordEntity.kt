package com.ysompo.englishwords.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val translationHe: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val orderIndex: Int
)
