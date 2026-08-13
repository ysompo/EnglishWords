package com.ysompo.englishwords.data

import android.content.Context

class WordSeeder(private val context: Context, private val wordDao: WordDao) {
    // Re-syncs (not just first-run seeds) whenever the bundled word count changes, so a child
    // who already has words seeded on their device picks up newly added words after an app
    // update - REPLACE conflict strategy means existing ids just get their content refreshed,
    // it doesn't touch the separate learning-progress table, so already-learned words stay learned.
    suspend fun seedIfNeeded() {
        val words = WordJsonLoader.loadFromAssets(context)
        if (wordDao.count() != words.size) {
            wordDao.insertAll(words)
        }
    }
}
