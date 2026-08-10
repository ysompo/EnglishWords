package com.ysompo.englishwords.logic

import com.ysompo.englishwords.data.WordEntity

enum class QuestionType { TRANSLATION_CHOICE, SENTENCE_COMPLETION }

data class QuizQuestion(
    val type: QuestionType,
    val promptWord: WordEntity,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)
