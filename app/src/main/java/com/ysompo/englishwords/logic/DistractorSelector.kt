package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity
import kotlin.random.Random

object DistractorSelector {

    fun translationDistractors(
        correct: WordEntity,
        pool: List<WordEntity>,
        count: Int = 3,
        random: Random = Random
    ): List<String> {
        val samePartOfSpeech = pool.filter {
            it.id != correct.id && it.partOfSpeech == correct.partOfSpeech && it.translationHe != correct.translationHe
        }.shuffled(random)

        val others = pool.filter {
            it.id != correct.id && it.partOfSpeech != correct.partOfSpeech && it.translationHe != correct.translationHe
        }.shuffled(random)

        return (samePartOfSpeech + others).distinctBy { it.translationHe }.take(count).map { it.translationHe }
    }

    fun sentenceCompletionDistractors(
        correct: WordEntity,
        pool: List<WordEntity>,
        count: Int = 3,
        random: Random = Random
    ): List<String> {
        val samePartOfSpeech = pool.filter {
            it.id != correct.id && it.partOfSpeech == correct.partOfSpeech && it.word != correct.word
        }.shuffled(random)

        return samePartOfSpeech.distinctBy { it.word }.take(count).map { it.word }
    }
}
