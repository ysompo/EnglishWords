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

    @Test
    fun `effectiveWeeklyStatuses derives a starred week from daily completions alone, with no weekly quiz taken`() = runBlocking {
        // Sunday 2026-08-09 through Tuesday 2026-08-11: 3 daily completions, no weekly quiz ever taken.
        repository.recordDailyCompletion(LocalDate.of(2026, 8, 9), learningDone = true, quizDone = true, quizScore = 5)
        repository.recordDailyCompletion(LocalDate.of(2026, 8, 10), learningDone = true, quizDone = true, quizScore = 5)
        repository.recordDailyCompletion(LocalDate.of(2026, 8, 11), learningDone = true, quizDone = true, quizScore = 5)

        val statuses = repository.effectiveWeeklyStatuses()

        assertThat(statuses).hasSize(1)
        assertThat(statuses.first().weekStartDate).isEqualTo("2026-08-09")
        assertThat(statuses.first().daysCompleted).isEqualTo(3)
        assertThat(statuses.first().starEarned).isTrue()
        assertThat(statuses.first().weeklyQuizScore).isNull()
    }

    @Test
    fun `effectiveWeeklyStatuses keeps the stored weekly quiz score when one was taken`() = runBlocking {
        repository.recordDailyCompletion(LocalDate.of(2026, 8, 9), learningDone = true, quizDone = true, quizScore = 5)
        repository.recordWeeklyStatus(LocalDate.of(2026, 8, 9), daysCompleted = 1, starEarned = false, quizScore = 4)

        val statuses = repository.effectiveWeeklyStatuses()

        assertThat(statuses).hasSize(1)
        assertThat(statuses.first().weeklyQuizScore).isEqualTo(4)
        assertThat(statuses.first().starEarned).isFalse()
    }

    @Test
    fun `learnedWordIdsByRecency returns most recently learned word first`() = runBlocking {
        repository.markWordsLearned(listOf(1, 2), LocalDate.of(2026, 8, 9))
        repository.markWordsLearned(listOf(3), LocalDate.of(2026, 8, 10))

        assertThat(repository.learnedWordIdsByRecency().first()).isEqualTo(3)
    }
}
