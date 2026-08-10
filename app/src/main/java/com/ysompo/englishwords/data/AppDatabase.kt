package com.ysompo.englishwords.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WordEntity::class,
        LearningProgressEntity::class,
        DailyCompletionEntity::class,
        WeeklyStatusEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun learningProgressDao(): LearningProgressDao
    abstract fun dailyCompletionDao(): DailyCompletionDao
    abstract fun weeklyStatusDao(): WeeklyStatusDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "english_words.db"
                ).build().also { INSTANCE = it }
            }
    }
}
