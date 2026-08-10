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
class ProgressDaosTest {

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
    fun `learning progress tracks learned word ids`() = runBlocking {
        db.learningProgressDao().insert(LearningProgressEntity(wordId = 1, learnedDate = "2026-08-10"))
        db.learningProgressDao().insert(LearningProgressEntity(wordId = 2, learnedDate = "2026-08-10"))

        assertThat(db.learningProgressDao().getLearnedWordIds()).containsExactly(1, 2)
        assertThat(db.learningProgressDao().countLearned()).isEqualTo(2)
    }

    @Test
    fun `daily completion upsert and range query`() = runBlocking {
        db.dailyCompletionDao().upsert(DailyCompletionEntity("2026-08-09", learningDone = true, quizDone = true, quizScore = 5))
        db.dailyCompletionDao().upsert(DailyCompletionEntity("2026-08-10", learningDone = true, quizDone = false, quizScore = 0))

        val range = db.dailyCompletionDao().getBetween("2026-08-09", "2026-08-13")
        assertThat(range).hasSize(2)

        val single = db.dailyCompletionDao().getByDate("2026-08-10")
        assertThat(single?.quizDone).isFalse()
    }

    @Test
    fun `weekly status upsert and lookup`() = runBlocking {
        db.weeklyStatusDao().upsert(WeeklyStatusEntity("2026-08-09", daysCompleted = 4, starEarned = true, weeklyQuizScore = 4))

        val result = db.weeklyStatusDao().getByWeekStart("2026-08-09")
        assertThat(result?.starEarned).isTrue()
        assertThat(db.weeklyStatusDao().getAll()).hasSize(1)
    }
}
