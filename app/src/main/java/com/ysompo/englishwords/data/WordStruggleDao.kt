package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordStruggleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WordStruggleEntity)

    @Query("SELECT * FROM word_struggles")
    suspend fun getAll(): List<WordStruggleEntity>
}
