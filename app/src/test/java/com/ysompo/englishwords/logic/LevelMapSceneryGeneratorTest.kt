package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelMapSceneryGeneratorTest {

    @Test
    fun `decorationsFor is deterministic for the same level`() {
        assertThat(LevelMapSceneryGenerator.decorationsFor(42))
            .isEqualTo(LevelMapSceneryGenerator.decorationsFor(42))
    }

    @Test
    fun `decorationsFor returns one or two decorations`() {
        (1..50).forEach { level ->
            assertThat(LevelMapSceneryGenerator.decorationsFor(level).size).isAnyOf(1, 2)
        }
    }

    @Test
    fun `decorationsFor keeps positions and scale within expected bounds`() {
        (1..50).forEach { level ->
            LevelMapSceneryGenerator.decorationsFor(level).forEach { decoration ->
                assertThat(decoration.xFraction).isAtLeast(0.08f)
                assertThat(decoration.xFraction).isAtMost(0.92f)
                assertThat(decoration.scale).isAtLeast(0.7f)
                assertThat(decoration.scale).isAtMost(1.3f)
            }
        }
    }
}
