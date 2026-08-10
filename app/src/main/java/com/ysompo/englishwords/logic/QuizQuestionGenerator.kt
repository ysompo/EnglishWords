package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity
import kotlin.random.Random

object QuizQuestionGenerator {

    fun dailyQuiz(learnedToday: List<WordEntity>, wordPool: List<WordEntity>, random: Random = Random): List<QuizQuestion> {
        return learnedToday.map { word -> buildQuestion(word, wordPool, useSentence = random.nextBoolean(), random) }
    }

    fun weeklyQuiz(
        candidateWords: List<WordEntity>,
        wordPool: List<WordEntity>,
        questionCount: Int = 5,
        random: Random = Random
    ): List<QuizQuestion> {
        return candidateWords.shuffled(random).take(questionCount).map { word ->
            buildQuestion(word, wordPool, useSentence = false, random)
        }
    }

    private fun buildQuestion(word: WordEntity, wordPool: List<WordEntity>, useSentence: Boolean, random: Random): QuizQuestion {
        return if (useSentence) {
            val distractors = DistractorSelector.sentenceCompletionDistractors(word, wordPool, count = 3, random = random)
            QuizQuestion(
                type = QuestionType.SENTENCE_COMPLETION,
                promptWord = word,
                questionText = word.exampleSentence,
                options = (distractors + word.word).shuffled(random),
                correctAnswer = word.word
            )
        } else {
            val distractors = DistractorSelector.translationDistractors(word, wordPool, count = 3, random = random)
            QuizQuestion(
                type = QuestionType.TRANSLATION_CHOICE,
                promptWord = word,
                questionText = word.word,
                options = (distractors + word.translationHe).shuffled(random),
                correctAnswer = word.translationHe
            )
        }
    }
}
