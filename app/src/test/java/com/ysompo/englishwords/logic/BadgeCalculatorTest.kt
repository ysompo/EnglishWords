package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BadgeCalculatorTest {

    @Test
    fun `unlockedBadges returns only badges at or below the learned count`() {
        val unlocked = BadgeCalculator.unlockedBadges(learnedCount = 30)

        assertThat(unlocked.map { it.id }).containsExactly("words_10", "words_25").inOrder()
    }

    @Test
    fun `unlockedBadges is empty below the first threshold`() {
        assertThat(BadgeCalculator.unlockedBadges(learnedCount = 5)).isEmpty()
    }

    @Test
    fun `unlockedBadges returns all badges when learned count reaches the max`() {
        val unlocked = BadgeCalculator.unlockedBadges(learnedCount = 1000)

        assertThat(unlocked).hasSize(BadgeCalculator.ALL_BADGES.size)
    }

    @Test
    fun `nextBadge returns the first locked badge, or null when all are unlocked`() {
        assertThat(BadgeCalculator.nextBadge(learnedCount = 30)?.id).isEqualTo("words_50")
        assertThat(BadgeCalculator.nextBadge(learnedCount = 1000)).isNull()
    }
}
