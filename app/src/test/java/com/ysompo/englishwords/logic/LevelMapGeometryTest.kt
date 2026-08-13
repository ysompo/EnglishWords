package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelMapGeometryTest {

    @Test
    fun `windowRange clamps to level 1 near the start`() {
        val range = LevelMapGeometry.windowRange(currentLevel = 2, totalLevels = 200, before = 12, after = 12)
        assertThat(range.first).isEqualTo(1)
        assertThat(range.last).isEqualTo(14)
    }

    @Test
    fun `windowRange clamps to totalLevels near the end`() {
        val range = LevelMapGeometry.windowRange(currentLevel = 198, totalLevels = 200, before = 12, after = 12)
        assertThat(range.first).isEqualTo(186)
        assertThat(range.last).isEqualTo(200)
    }

    @Test
    fun `windowRange centers on currentLevel in the middle of the path`() {
        val range = LevelMapGeometry.windowRange(currentLevel = 100, totalLevels = 200, before = 12, after = 12)
        assertThat(range).isEqualTo(88..112)
    }

    @Test
    fun `windowRange is empty when there are no levels`() {
        assertThat(LevelMapGeometry.windowRange(currentLevel = 1, totalLevels = 0)).isEqualTo(IntRange.EMPTY)
    }

    @Test
    fun `nodePositions places one entry per level with increasing y`() {
        val positions = LevelMapGeometry.nodePositions(1..5, nodeSpacingPx = 100f, topPaddingPx = 50f)
        assertThat(positions).hasSize(5)
        assertThat(positions.map { it.level }).isEqualTo(listOf(1, 2, 3, 4, 5))
        assertThat(positions[0].centerYPx).isEqualTo(50f)
        assertThat(positions[4].centerYPx).isEqualTo(450f)
    }

    @Test
    fun `nodePositions keeps x fraction within the zigzag amplitude around center`() {
        val positions = LevelMapGeometry.nodePositions(1..12, nodeSpacingPx = 100f, topPaddingPx = 0f)
        positions.forEach {
            assertThat(it.centerXFraction).isAtLeast(0.15f)
            assertThat(it.centerXFraction).isAtMost(0.85f)
        }
    }

    @Test
    fun `contentHeightPx accounts for top and bottom padding`() {
        val height = LevelMapGeometry.contentHeightPx(nodeCount = 3, nodeSpacingPx = 100f, topPaddingPx = 20f, bottomPaddingPx = 30f)
        assertThat(height).isEqualTo(250f)
    }

    @Test
    fun `contentHeightPx is zero for no nodes`() {
        assertThat(LevelMapGeometry.contentHeightPx(0, 100f, 20f, 30f)).isEqualTo(0f)
    }

    @Test
    fun `levelAt finds the nearest node within hit radius`() {
        val positions = listOf(
            LevelNodePosition(1, 0.5f, 100f),
            LevelNodePosition(2, 0.2f, 300f)
        )
        val hit = LevelMapGeometry.levelAt(xPx = 200f, yPx = 100f, positions = positions, viewWidthPx = 400f, hitRadiusPx = 50f)
        assertThat(hit).isEqualTo(1)
    }

    @Test
    fun `levelAt returns null when the tap misses every node`() {
        val positions = listOf(LevelNodePosition(1, 0.5f, 100f))
        val hit = LevelMapGeometry.levelAt(xPx = 0f, yPx = 0f, positions = positions, viewWidthPx = 400f, hitRadiusPx = 30f)
        assertThat(hit).isNull()
    }

    @Test
    fun `scrollTargetY centers the node but clamps to valid scroll range`() {
        assertThat(LevelMapGeometry.scrollTargetY(nodeCenterYPx = 500f, viewportHeightPx = 800f, contentHeightPx = 2000f))
            .isEqualTo(100)
        assertThat(LevelMapGeometry.scrollTargetY(nodeCenterYPx = 50f, viewportHeightPx = 800f, contentHeightPx = 2000f))
            .isEqualTo(0)
        assertThat(LevelMapGeometry.scrollTargetY(nodeCenterYPx = 1950f, viewportHeightPx = 800f, contentHeightPx = 2000f))
            .isEqualTo(1200)
    }
}
