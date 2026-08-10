package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyCompletionEntity)

    @Query("SELECT * FROM daily_completion WHERE date = :date")
    suspend fun getByDate(date: String): DailyCompletionEntity?

    @Query("SELECT * FROM daily_completion WHERE date BETWEEN :start AND :end")
    suspend fun getBetween(start: String, end: String): List<DailyCompletionEntity>
}
