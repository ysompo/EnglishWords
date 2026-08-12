package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WordleEvaluatorTest {

    @Test
    fun `exact match is all CORRECT`() {
        val result = WordleEvaluator.evaluate("happy", "happy")

        assertThat(result).containsExactly(
            LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT
        ).inOrder()
    }

    @Test
    fun `letters in wrong position are PRESENT, letters not in target are ABSENT`() {
        // target "happy": h-a-p-p-y ; guess "apple": a-p-p-l-e
        val result = WordleEvaluator.evaluate("apple", "happy")

        assertThat(result).containsExactly(
            LetterState.PRESENT, // a -> present (index 1 in target)
            LetterState.PRESENT, // p -> present (a p is used at index 2, but this p matches a different p)
            LetterState.CORRECT, // p -> correct (index 2 matches exactly)
            LetterState.ABSENT,  // l -> not in target
            LetterState.ABSENT   // e -> not in target
        ).inOrder()
    }

    @Test
    fun `duplicate letters in the guess are not over-counted beyond how many times they appear in the target`() {
        // target "happy" has exactly two p's; a guess of all p's should only mark the two
        // positions that are genuinely correct, not PRESENT for the rest.
        val result = WordleEvaluator.evaluate("ppppp", "happy")

        assertThat(result).containsExactly(
            LetterState.ABSENT,
            LetterState.ABSENT,
            LetterState.CORRECT,
            LetterState.CORRECT,
            LetterState.ABSENT
        ).inOrder()
    }

    @Test
    fun `is case-insensitive`() {
        val result = WordleEvaluator.evaluate("HAPPY", "happy")

        assertThat(result).containsExactly(
            LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT, LetterState.CORRECT
        ).inOrder()
    }
}
