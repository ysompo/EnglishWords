package com.ysompo.englishwords.settings

// The word bank is ordered by frequency (word 1 = most common), so "starting further in"
// simply means skipping the easiest/most basic words entirely rather than teaching them.
enum class ProficiencyLevel(val startingWordIndex: Int, val labelHe: String) {
    BEGINNER(0, "מתחילים"),
    INTERMEDIATE(200, "בינוני"),
    ADVANCED(500, "מתקדם")
}
