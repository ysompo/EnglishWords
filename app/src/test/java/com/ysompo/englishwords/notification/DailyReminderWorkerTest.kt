package com.ysompo.englishwords.notification

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.AppDatabase
import com.ysompo.englishwords.repo.ProgressRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class DailyReminderWorkerTest {

    private lateinit var db: AppDatabase
    private lateinit var progressRepository: ProgressRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        progressRepository = ProgressRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `shouldNotify is true when today has no completion record`() = runBlocking {
        val result = DailyReminderWorker.shouldNotify(progressRepository, LocalDate.of(2026, 8, 10))
        assertThat(result).isTrue()
    }

    @Test
    fun `shouldNotify is false when today is already complete`() = runBlocking {
        val today = LocalDate.of(2026, 8, 10)
        progressRepository.recordDailyCompletion(today, learningDone = true, quizDone = true, quizScore = 5)

        val result = DailyReminderWorker.shouldNotify(progressRepository, today)

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldNotify is true when learning is done but quiz is not`() = runBlocking {
        val today = LocalDate.of(2026, 8, 10)
        progressRepository.recordDailyCompletion(today, learningDone = true, quizDone = false, quizScore = 0)

        val result = DailyReminderWorker.shouldNotify(progressRepository, today)

        assertThat(result).isTrue()
    }
}
