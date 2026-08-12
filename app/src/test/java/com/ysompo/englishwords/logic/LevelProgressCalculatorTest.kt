package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelProgressCalculatorTest {

    @Test
    fun `levelsCompleted counts full batches of words learned`() {
        assertThat(LevelProgressCalculator.levelsCompleted(0)).isEqualTo(0)
        assertThat(LevelProgressCalculator.levelsCompleted(4)).isEqualTo(0)
        assertThat(LevelProgressCalculator.levelsCompleted(5)).isEqualTo(1)
        assertThat(LevelProgressCalculator.levelsCompleted(12)).isEqualTo(2)
    }

    @Test
    fun `currentLevelNumber is one more than levels completed`() {
        assertThat(LevelProgressCalculator.currentLevelNumber(0)).isEqualTo(1)
        assertThat(LevelProgressCalculator.currentLevelNumber(5)).isEqualTo(2)
        assertThat(LevelProgressCalculator.currentLevelNumber(12)).isEqualTo(3)
    }

    @Test
    fun `totalLevels rounds up so a partial last batch still counts as a level`() {
        assertThat(LevelProgressCalculator.totalLevels(1000)).isEqualTo(200)
        assertThat(LevelProgressCalculator.totalLevels(1002)).isEqualTo(201)
        assertThat(LevelProgressCalculator.totalLevels(1)).isEqualTo(1)
    }
}
