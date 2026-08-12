package com.ysompo.englishwords.logic

object UpdateVersionComparator {

    fun isNewer(remoteVersion: String, currentVersion: String): Boolean {
        val remote = parse(remoteVersion)
        val current = parse(currentVersion)
        for (i in 0 until maxOf(remote.size, current.size)) {
            val r = remote.getOrElse(i) { 0 }
            val c = current.getOrElse(i) { 0 }
            if (r != c) return r > c
        }
        return false
    }

    private fun parse(version: String): List<Int> =
        version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .split(".")
            .map { segment -> segment.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }
}
