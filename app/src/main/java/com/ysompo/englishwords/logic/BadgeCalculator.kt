package com.ysompo.englishwords.logic

object BadgeCalculator {
    val ALL_BADGES: List<Badge> = listOf(
        Badge("words_10", 10, "10 מילים!"),
        Badge("words_25", 25, "25 מילים!"),
        Badge("words_50", 50, "חצי מאה!"),
        Badge("words_100", 100, "100 מילים!"),
        Badge("words_200", 200, "200 מילים!"),
        Badge("words_350", 350, "350 מילים!"),
        Badge("words_500", 500, "חצי הדרך!"),
        Badge("words_750", 750, "750 מילים!"),
        Badge("words_1000", 1000, "אלוף האנגלית!")
    )

    fun unlockedBadges(learnedCount: Int): List<Badge> = ALL_BADGES.filter { learnedCount >= it.threshold }

    fun nextBadge(learnedCount: Int): Badge? = ALL_BADGES.firstOrNull { learnedCount < it.threshold }
}
