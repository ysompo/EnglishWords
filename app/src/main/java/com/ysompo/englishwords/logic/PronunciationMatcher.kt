package com.ysompo.englishwords.logic

object PronunciationMatcher {

    fun isMatch(recognizedText: String, targetWord: String): Boolean {
        val recognized = normalize(recognizedText)
        val target = normalize(targetWord)
        if (recognized.isEmpty()) return false
        if (recognized == target) return true

        val tolerance = toleranceFor(target)
        if (recognized.split(" ").any { levenshtein(it, target) <= tolerance }) return true

        return levenshtein(recognized, target) <= tolerance
    }

    // More forgiving than a strict phonetic check on purpose: this is a young child's first
    // attempts at pronunciation, so we'd rather accept a slightly-off attempt than discourage
    // them with a false "wrong" on a word they basically got right. Short words stay stricter
    // (a 1-2 letter target is "close" to almost anything under a loose tolerance), longer words
    // scale up more generously since STT noise tends to add/drop more characters on them.
    private fun toleranceFor(target: String): Int = when {
        target.length <= 2 -> 1
        target.length <= 5 -> 2
        else -> (target.length + 2) / 3
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
