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
    fun `unrelated word is not a match`() {
        assertThat(PronunciationMatcher.isMatch("banana", "happy")).isFalse()
    }

    @Test
    fun `empty recognized text is never a match`() {
        assertThat(PronunciationMatcher.isMatch("", "happy")).isFalse()
    }
}
