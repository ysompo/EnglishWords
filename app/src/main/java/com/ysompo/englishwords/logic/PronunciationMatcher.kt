package com.ysompo.englishwords.logic

object PronunciationMatcher {

    fun isMatch(recognizedText: String, targetWord: String): Boolean {
        val recognized = normalize(recognizedText)
        val target = normalize(targetWord)
        if (recognized.isEmpty()) return false
        if (recognized == target) return true
        if (recognized.split(" ").any { it == target }) return true

        val distance = levenshtein(recognized, target)
        val tolerance = maxOf(1, target.length / 4)
        return distance <= tolerance
    }

    private fun normalize(text: String): String =
        text.trim().lowercase().replace(Regex("[^a-z ]"), "")

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }
}
