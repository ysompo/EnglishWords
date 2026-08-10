package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity

object DailyLessonSelector {
    const val WORDS_PER_DAY = 5

    fun nextWordsToLearn(allWordsOrderedByIndex: List<WordEntity>, learnedWordIds: Set<Int>): List<WordEntity> {
        return allWordsOrderedByIndex
            .sortedBy { it.orderIndex }
            .filter { it.id !in learnedWordIds }
            .take(WORDS_PER_DAY)
    }
}
