package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PronunciationMatcherTest {

    @Test
    fun `exact match (case-insensitive) is a match`() {
        assertThat(PronunciationMatcher.isMatch("Happy", "happy")).isTrue()
        assertThat(PronunciationMatcher.isMatch("HAPPY", "happy")).isTrue()
    }

    @Test
    fun `recognized phrase containing the target word as a separate word is a match`() {
        assertThat(PronunciationMatcher.isMatch("the happy", "happy")).isTrue()
    }

    @Test
    fun `close mispronunciation within tolerance is a match`() {
        assertThat(PronunciationMatcher.isMatch("hapy", "happy")).isTrue() // 1 char off, tolerance >= 1
    }

    @Test
    fun `mispronunciation two characters off is now also a match (more forgiving)`() {
        assertThat(PronunciationMatcher.isMatch("hapi", "happy")).isTrue() // 2 edits off "happy"
    }

    @Test
    fun `a slightly misheard word inside a longer phrase still matches`() {
        // "happpy" (extra p) is 1 edit from "happy", buried inside a 3-word phrase.
        assertThat(PronunciationMatcher.isMatch("a happpy day", "happy")).isTrue()
    }

    @Test
    fun `unrelated word is not a match`() {
        assertThat(PronunciationMatcher.isMatch("banana", "happy")).isFalse()
    }

    @Test
    fun `short target words stay reasonably strict so they are not trivially satisfied`() {
        assertThat(PronunciationMatcher.isMatch("cats", "it")).isFalse()
    }

    @Test
    fun `empty recognized text is never a match`() {
        assertThat(PronunciationMatcher.isMatch("", "happy")).isFalse()
    }
}
