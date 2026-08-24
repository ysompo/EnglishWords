package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LearningProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: LearningProgressEntity)

    @Query("SELECT wordId FROM learning_progress")
    suspend fun getLearnedWordIds(): List<Int>

    @Query("SELECT wordId FROM learning_progress ORDER BY learnedDate DESC")
    suspend fun getLearnedWordIdsByRecency(): List<Int>

    @Query("SELECT COUNT(*) FROM learning_progress")
    suspend fun countLearned(): Int
}
