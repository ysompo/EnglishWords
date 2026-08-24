package com.ysompo.englishwords.logic

// A "level" is exactly one daily lesson batch (5 new words + the quiz on them) - same content
// unit as before, just framed as a game-style level to pass rather than a calendar day. Nothing
// stops a child from completing multiple levels in one sitting; the weekly star/monthly reward
// still depends on which calendar days had activity (see StreakCalculator), not level count.
object LevelProgressCalculator {
    const val WORDS_PER_LEVEL = 5

    fun levelsCompleted(learnedWordCount: Int): Int = learnedWordCount / WORDS_PER_LEVEL

    fun currentLevelNumber(learnedWordCount: Int): Int = levelsCompleted(learnedWordCount) + 1

    fun totalLevels(totalWordCount: Int): Int =
        (totalWordCount + WORDS_PER_LEVEL - 1) / WORDS_PER_LEVEL

    // How many of the current (not-yet-completed) level's words are already learned - e.g. 1 out
    // of 5. currentLevelNumber only ticks up once this hits WORDS_PER_LEVEL, so a child who has
    // done N daily lessons but had words skipped along the way (see LearnWordsViewModel's
    // pronunciation-skip path) will see a currentLevelNumber lower than N, with the remainder
    // showing here rather than vanishing.
    fun wordsCompletedInCurrentLevel(learnedWordCount: Int): Int = learnedWordCount % WORDS_PER_LEVEL
}
