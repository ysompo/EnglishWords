package com.ysompo.englishwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeeklyStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeeklyStatusEntity)

    @Query("SELECT * FROM weekly_status ORDER BY weekStartDate ASC")
    suspend fun getAll(): List<WeeklyStatusEntity>

    @Query("SELECT * FROM weekly_status WHERE weekStartDate = :weekStartDate")
    suspend fun getByWeekStart(weekStartDate: String): WeeklyStatusEntity?
}
