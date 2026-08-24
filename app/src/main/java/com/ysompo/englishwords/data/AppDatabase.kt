package com.ysompo.englishwords.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WordEntity::class,
        LearningProgressEntity::class,
        DailyCompletionEntity::class,
        WeeklyStatusEntity::class,
        WordStruggleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun learningProgressDao(): LearningProgressDao
    abstract fun dailyCompletionDao(): DailyCompletionDao
    abstract fun weeklyStatusDao(): WeeklyStatusDao
    abstract fun wordStruggleDao(): WordStruggleDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Adds word_struggles (words the child failed to translate/pronounce or got wrong on a
        // quiz) without touching existing tables - real installs already carry weeks of progress,
        // so this must not fall back to destructive migration.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `word_struggles` (`wordId` INTEGER NOT NULL, `lastAttemptDate` TEXT NOT NULL, PRIMARY KEY(`wordId`))"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "english_words.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
