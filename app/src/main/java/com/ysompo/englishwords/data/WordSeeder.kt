package com.ysompo.englishwords.data

import android.content.Context

class WordSeeder(private val context: Context, private val wordDao: WordDao) {
    suspend fun seedIfNeeded() {
        if (wordDao.count() == 0) {
            wordDao.insertAll(WordJsonLoader.loadFromAssets(context))
        }
    }
}
