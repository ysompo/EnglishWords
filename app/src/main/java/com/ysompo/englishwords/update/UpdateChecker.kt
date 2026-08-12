package com.ysompo.englishwords.update

import com.ysompo.englishwords.logic.GitHubReleaseParser
import com.ysompo.englishwords.logic.LatestReleaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    private const val API_URL = "https://api.github.com/repos/ysompo/EnglishWords/releases/latest"

    suspend fun fetchLatestRelease(): LatestReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(API_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                GitHubReleaseParser.parse(body)
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            null
        }
    }
}
