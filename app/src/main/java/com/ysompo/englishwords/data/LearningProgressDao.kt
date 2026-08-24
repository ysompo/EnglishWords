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

    @Query("SELECT * FROM learning_progress")
    suspend fun getAll(): List<LearningProgressEntity>

    @Query("SELECT COUNT(*) FROM learning_progress")
    suspend fun countLearned(): Int
}
