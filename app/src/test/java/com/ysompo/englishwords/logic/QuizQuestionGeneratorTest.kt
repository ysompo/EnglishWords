package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.WordEntity
import org.junit.Test
import kotlin.random.Random

class QuizQuestionGeneratorTest {

    private val pool = listOf(
        WordEntity(1, "run", "לרוץ", "verb", "I ___ fast.", 1),
        WordEntity(2, "eat", "לאכול", "verb", "I ___ lunch.", 2),
        WordEntity(3, "jump", "לקפוץ", "verb", "I ___ high.", 3),
        WordEntity(4, "walk", "ללכת", "verb", "I ___ to school.", 4),
        WordEntity(5, "happy", "שמח", "adjective", "I am ___.", 5),
        WordEntity(6, "sad", "עצוב", "adjective", "I am ___.", 6),
        WordEntity(7, "big", "גדול", "adjective", "The house is ___.", 7),
        WordEntity(8, "small", "קטן", "adjective", "The cat is ___.", 8)
    )

    @Test
    fun `dailyQuiz returns one question per learned word with 4 options including the correct answer`() {
        val learnedToday = pool.take(2)

        val questions = QuizQuestionGenerator.dailyQuiz(learnedToday, pool, random = Random(3))

        assertThat(questions).hasSize(2)
        questions.forEach { q ->
            assertThat(q.options).hasSize(4)
            assertThat(q.options).contains(q.correctAnswer)
        }
    }

    @Test
    fun `weeklyQuiz returns translation-choice questions only, capped at questionCount`() {
        val questions = QuizQuestionGenerator.weeklyQuiz(pool, pool, questionCount = 3, random = Random(9))

        assertThat(questions).hasSize(3)
        questions.forEach { q ->
            assertThat(q.type).isEqualTo(QuestionType.TRANSLATION_CHOICE)
            assertThat(q.options).contains(q.correctAnswer)
        }
    }

    @Test
    fun `weeklyQuiz caps at available words when fewer than questionCount`() {
        val small = pool.take(2)

        val questions = QuizQuestionGenerator.weeklyQuiz(small, pool, questionCount = 5, random = Random(2))

        assertThat(questions).hasSize(2)
    }
}
