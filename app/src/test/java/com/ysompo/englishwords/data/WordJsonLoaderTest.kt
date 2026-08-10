package com.ysompo.englishwords.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WordJsonLoaderTest {

    private val sampleJson = """
        [
          {"id": 1, "word": "the", "translation_he": "ה-", "part_of_speech": "determiner", "example_sentence": "I saw ___ dog.", "order_index": 1},
          {"id": 2, "word": "and", "translation_he": "ו-", "part_of_speech": "conjunction", "example_sentence": "tea ___ coffee.", "order_index": 2}
        ]
    """.trimIndent()

    @Test
    fun `parses all fields correctly`() {
        val words = WordJsonLoader.parse(sampleJson)

        assertThat(words).hasSize(2)
        assertThat(words[0]).isEqualTo(
            WordEntity(id = 1, word = "the", translationHe = "ה-", partOfSpeech = "determiner", exampleSentence = "I saw ___ dog.", orderIndex = 1)
        )
        assertThat(words[1].word).isEqualTo("and")
    }

    @Test
    fun `parses empty array as empty list`() {
        assertThat(WordJsonLoader.parse("[]")).isEmpty()
    }
}
