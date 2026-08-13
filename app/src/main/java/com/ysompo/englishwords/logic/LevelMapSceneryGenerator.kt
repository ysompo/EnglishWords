package com.ysompo.englishwords.logic

import kotlin.random.Random

enum class DecorationType { HILL, TREE, CLOUD }

data class Decoration(
    val type: DecorationType,
    val xFraction: Float,
    val yOffsetPx: Float,
    val scale: Float
)

// Deterministic decorative scenery per level "segment" of the map, seeded from the level
// number so the same level always renders the same hills/trees/clouds - it must not jitter
// on redraw (e.g. every scroll-triggered invalidate, or every HomeActivity.onResume reload).
object LevelMapSceneryGenerator {
    private const val SEED_MULTIPLIER = 7919L

    fun decorationsFor(level: Int): List<Decoration> {
        val random = Random(level * SEED_MULTIPLIER)
        val count = 1 + random.nextInt(2)
        return List(count) {
            val type = DecorationType.entries[random.nextInt(DecorationType.entries.size)]
            Decoration(
                type = type,
                xFraction = 0.08f + random.nextFloat() * 0.84f,
                yOffsetPx = -40f + random.nextFloat() * 80f,
                scale = 0.7f + random.nextFloat() * 0.6f
            )
        }
    }
}
