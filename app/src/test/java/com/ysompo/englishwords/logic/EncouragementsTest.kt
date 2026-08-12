package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EncouragementsTest {

    @Test
    fun `random always returns one of the known phrases`() {
        repeat(50) {
            assertThat(Encouragements.PHRASES).contains(Encouragements.random())
        }
    }

    @Test
    fun `there is more than one phrase, so it doesn't always say the same thing`() {
        assertThat(Encouragements.PHRASES.size).isGreaterThan(1)
    }
}
