package com.ysompo.englishwords.repo

import android.content.Context
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.data.WordEntity
import com.ysompo.englishwords.data.WordSeeder

class WordRepository(private val db: AppDatabase) {
    suspend fun ensureSeeded(context: Context) {
        WordSeeder(context, db.wordDao()).seedIfNeeded()
    }

    suspend fun allWordsOrdered(): List<WordEntity> = db.wordDao().getAllOrdered()
}
