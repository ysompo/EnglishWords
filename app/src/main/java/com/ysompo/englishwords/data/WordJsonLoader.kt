package com.ysompo.englishwords.data

import android.content.Context
import org.json.JSONArray

object WordJsonLoader {

    fun loadFromAssets(context: Context, assetName: String = "words.json"): List<WordEntity> {
        val jsonText = context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parse(jsonText)
    }

    fun parse(jsonText: String): List<WordEntity> {
        val array = JSONArray(jsonText)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            WordEntity(
                id = obj.getInt("id"),
                word = obj.getString("word"),
                translationHe = obj.getString("translation_he"),
                partOfSpeech = obj.getString("part_of_speech"),
                exampleSentence = obj.getString("example_sentence"),
                orderIndex = obj.getInt("order_index")
            )
        }
    }
}
