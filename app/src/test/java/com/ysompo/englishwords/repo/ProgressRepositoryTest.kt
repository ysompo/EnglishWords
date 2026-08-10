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
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class ProgressRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ProgressRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProgressRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `markWordsLearned then learnedWordIds and learnedWordCount reflect them`() = runBlocking {
        repository.markWordsLearned(listOf(1, 2, 3), LocalDate.of(2026, 8, 10))

        assertThat(repository.learnedWordIds()).containsExactly(1, 2, 3)
        assertThat(repository.learnedWordCount()).isEqualTo(3)
    }

    @Test
    fun `recordDailyCompletion then completionForDate and completionsForWeek return it`() = runBlocking {
        val monday = LocalDate.of(2026, 8, 10)
        repository.recordDailyCompletion(monday, learningDone = true, quizDone = true, quizScore = 5)

        assertThat(repository.completionForDate(monday)?.quizScore).isEqualTo(5)

        val sunday = LocalDate.of(2026, 8, 9)
        assertThat(repository.completionsForWeek(sunday)).hasSize(1)
    }

    @Test
    fun `recordWeeklyStatus then allWeeklyStatuses returns it`() = runBlocking {
        repository.recordWeeklyStatus(LocalDate.of(2026, 8, 9), daysCompleted = 4, starEarned = true, quizScore = 5)

        val all = repository.allWeeklyStatuses()
        assertThat(all).hasSize(1)
        assertThat(all.first().starEarned).isTrue()
    }
}
