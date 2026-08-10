package com.ysompo.englishwords.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WordRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WordRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WordRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `ensureSeeded loads words once, allWordsOrdered returns them sorted`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        repository.ensureSeeded(context)
        repository.ensureSeeded(context) // calling twice must not duplicate rows

        val words = repository.allWordsOrdered()
        assertThat(words).hasSize(40)
        assertThat(words.first().orderIndex).isEqualTo(1)
    }
}
