package com.ysompo.englishwords.logic

object Encouragements {
    val PHRASES = listOf(
        "כל הכבוד!",
        "מעולה!",
        "אלוף!",
        "יופי!",
        "אתה מתקדם נהדר!",
        "וואו, איזה כיף!",
        "המשך ככה!",
        "מדהים!",
        "עבודה נהדרת!",
        "אתה שולט בזה!"
    )

    fun random(): String = PHRASES.random()
}
