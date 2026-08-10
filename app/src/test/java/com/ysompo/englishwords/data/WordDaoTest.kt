package com.ysompo.englishwords.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WordDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.wordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insertAll then getAllOrdered returns words sorted by orderIndex`() = runBlocking {
        val w1 = WordEntity(1, "the", "ה-", "determiner", "I saw ___ dog.", orderIndex = 2)
        val w2 = WordEntity(2, "and", "ו-", "conjunction", "tea ___ coffee.", orderIndex = 1)
        dao.insertAll(listOf(w1, w2))

        val result = dao.getAllOrdered()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(2)
        assertThat(result[1].id).isEqualTo(1)
    }

    @Test
    fun `count reflects number of inserted rows`() = runBlocking {
        assertThat(dao.count()).isEqualTo(0)
        dao.insertAll(listOf(WordEntity(1, "the", "ה-", "determiner", "s", 1)))
        assertThat(dao.count()).isEqualTo(1)
    }
}
