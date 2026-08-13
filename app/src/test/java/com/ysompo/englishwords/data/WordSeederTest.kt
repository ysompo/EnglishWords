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
class WordSeederTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `seedIfNeeded loads all words from assets on first run`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val seeder = WordSeeder(context, db.wordDao())

        seeder.seedIfNeeded()

        assertThat(db.wordDao().count()).isEqualTo(1286)
    }

    @Test
    fun `seedIfNeeded does nothing if words already exist`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val seeder = WordSeeder(context, db.wordDao())
        seeder.seedIfNeeded()
        val countAfterFirst = db.wordDao().count()

        seeder.seedIfNeeded()

        assertThat(db.wordDao().count()).isEqualTo(countAfterFirst)
    }

    @Test
    fun `seedIfNeeded resyncs stale word content when the bundled word count changes`() = runBlocking {
        // Simulates a device that already seeded an older word bank (e.g. before an app update
        // added new words) - the stale row for id 1 must be replaced with the bundled content,
        // not left behind, once the counts no longer match.
        db.wordDao().insertAll(
            listOf(
                WordEntity(
                    id = 1,
                    word = "stale",
                    translationHe = "ישן",
                    partOfSpeech = "noun",
                    exampleSentence = "___",
                    orderIndex = 1
                )
            )
        )
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        WordSeeder(context, db.wordDao()).seedIfNeeded()

        val word1 = db.wordDao().getById(1)
        assertThat(word1?.word).isEqualTo("the")
        assertThat(db.wordDao().count()).isEqualTo(1286)
    }
}
