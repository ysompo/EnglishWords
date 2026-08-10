package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.WordEntity
import org.junit.Test

class DailyLessonSelectorTest {

    private fun word(id: Int, orderIndex: Int) =
        WordEntity(id, "word$id", "he$id", "noun", "sentence ___.", orderIndex)

    @Test
    fun `returns first 5 words in orderIndex order when none learned`() {
        val words = (1..10).map { word(it, orderIndex = it) }.shuffled()

        val result = DailyLessonSelector.nextWordsToLearn(words, learnedWordIds = emptySet())

        assertThat(result.map { it.id }).containsExactly(1, 2, 3, 4, 5).inOrder()
    }

    @Test
    fun `skips already-learned words`() {
        val words = (1..10).map { word(it, orderIndex = it) }

        val result = DailyLessonSelector.nextWordsToLearn(words, learnedWordIds = setOf(1, 2, 3, 4, 5))

        assertThat(result.map { it.id }).containsExactly(6, 7, 8, 9, 10).inOrder()
    }

    @Test
    fun `returns fewer than 5 when fewer words remain`() {
        val words = (1..3).map { word(it, orderIndex = it) }

        val result = DailyLessonSelector.nextWordsToLearn(words, learnedWordIds = emptySet())

        assertThat(result).hasSize(3)
    }
}
