package com.ysompo.englishwords.settings

import android.content.Context

class DifficultySettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("difficulty_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LEVEL = "proficiency_level"
        val DEFAULT_LEVEL = ProficiencyLevel.BEGINNER
    }

    fun getLevel(): ProficiencyLevel {
        val savedName = prefs.getString(KEY_LEVEL, DEFAULT_LEVEL.name)
        return ProficiencyLevel.entries.find { it.name == savedName } ?: DEFAULT_LEVEL
    }

    fun setLevel(level: ProficiencyLevel) {
        prefs.edit().putString(KEY_LEVEL, level.name).apply()
    }
}
