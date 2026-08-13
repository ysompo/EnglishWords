package com.ysompo.englishwords.logic

import kotlin.math.sin

data class LevelNodePosition(
    val level: Int,
    val centerXFraction: Float,
    val centerYPx: Float
)

// Pure geometry for the Home screen's winding "level map" path - no Android framework
// dependencies, kept separate from LevelMapView so placement/hit-testing/scroll math stays
// unit-testable, matching this codebase's convention of pushing non-trivial math into logic/.
object LevelMapGeometry {
    const val DEFAULT_WINDOW_BEFORE = 12
    const val DEFAULT_WINDOW_AFTER = 12
    private const val ZIGZAG_PERIOD = 6
    private const val ZIGZAG_AMPLITUDE = 0.30f

    fun windowRange(
        currentLevel: Int,
        totalLevels: Int,
        before: Int = DEFAULT_WINDOW_BEFORE,
        after: Int = DEFAULT_WINDOW_AFTER
    ): IntRange {
        if (totalLevels <= 0) return IntRange.EMPTY
        val safeCurrent = currentLevel.coerceIn(1, totalLevels)
        val start = maxOf(1, safeCurrent - before)
        val end = minOf(totalLevels, safeCurrent + after)
        return start..end
    }

    fun nodePositions(levels: IntRange, nodeSpacingPx: Float, topPaddingPx: Float): List<LevelNodePosition> {
        if (levels.isEmpty()) return emptyList()
        return levels.mapIndexed { index, level ->
            val angle = 2.0 * Math.PI * (index % ZIGZAG_PERIOD) / ZIGZAG_PERIOD
            val xFraction = 0.5f + ZIGZAG_AMPLITUDE * sin(angle).toFloat()
            LevelNodePosition(level, xFraction, topPaddingPx + index * nodeSpacingPx)
        }
    }

    fun contentHeightPx(nodeCount: Int, nodeSpacingPx: Float, topPaddingPx: Float, bottomPaddingPx: Float): Float =
        if (nodeCount == 0) 0f else topPaddingPx + (nodeCount - 1) * nodeSpacingPx + bottomPaddingPx

    fun levelAt(
        xPx: Float,
        yPx: Float,
        positions: List<LevelNodePosition>,
        viewWidthPx: Float,
        hitRadiusPx: Float
    ): Int? {
        var bestLevel: Int? = null
        var bestDistSq = Float.MAX_VALUE
        for (pos in positions) {
            val centerX = pos.centerXFraction * viewWidthPx
            val dx = xPx - centerX
            val dy = yPx - pos.centerYPx
            val distSq = dx * dx + dy * dy
            if (distSq <= hitRadiusPx * hitRadiusPx && distSq < bestDistSq) {
                bestDistSq = distSq
                bestLevel = pos.level
            }
        }
        return bestLevel
    }

    fun scrollTargetY(nodeCenterYPx: Float, viewportHeightPx: Float, contentHeightPx: Float): Int {
        val raw = nodeCenterYPx - viewportHeightPx / 2f
        val maxScroll = maxOf(0f, contentHeightPx - viewportHeightPx)
        return raw.coerceIn(0f, maxScroll).toInt()
    }
}
