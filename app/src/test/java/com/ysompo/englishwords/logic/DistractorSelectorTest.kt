package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import com.ysompo.englishwords.data.WordEntity
import org.junit.Test
import kotlin.random.Random

class DistractorSelectorTest {

    private val pool = listOf(
        WordEntity(1, "run", "לרוץ", "verb", "I ___ fast.", 1),
        WordEntity(2, "eat", "לאכול", "verb", "I ___ lunch.", 2),
        WordEntity(3, "jump", "לקפוץ", "verb", "I ___ high.", 3),
        WordEntity(4, "happy", "שמח", "adjective", "I am ___.", 4),
        WordEntity(5, "sad", "עצוב", "adjective", "I am ___.", 5)
    )

    @Test
    fun `translationDistractors excludes the correct word and returns requested count`() {
        val correct = pool[0] // "run"

        val distractors = DistractorSelector.translationDistractors(correct, pool, count = 2, random = Random(42))

        assertThat(distractors).hasSize(2)
        assertThat(distractors).doesNotContain(correct.translationHe)
    }

    @Test
    fun `translationDistractors prefers same part of speech when enough candidates exist`() {
        val correct = pool[0] // verb "run"

        val distractors = DistractorSelector.translationDistractors(correct, pool, count = 2, random = Random(1))

        // Only 2 other verbs exist ("eat", "jump") - both should be chosen before any adjective.
        assertThat(distractors).containsExactly("לאכול", "לקפוץ")
    }

    @Test
    fun `sentenceCompletionDistractors only returns words with matching part of speech`() {
        val correct = pool[3] // adjective "happy"

        val distractors = DistractorSelector.sentenceCompletionDistractors(correct, pool, count = 1, random = Random(7))

        assertThat(distractors).containsExactly("sad")
    }
}
