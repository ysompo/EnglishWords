package com.ysompo.englishwords.logic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GitHubReleaseParserTest {

    private val sampleJson = """
        {
          "tag_name": "v1.2.0",
          "html_url": "https://github.com/ysompo/EnglishWords/releases/tag/v1.2.0",
          "assets": [
            {
              "name": "app-debug.apk",
              "browser_download_url": "https://github.com/ysompo/EnglishWords/releases/download/v1.2.0/app-debug.apk"
            },
            {
              "name": "source.zip",
              "browser_download_url": "https://github.com/ysompo/EnglishWords/releases/download/v1.2.0/source.zip"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `parses tag, release url, and finds the apk asset`() {
        val info = GitHubReleaseParser.parse(sampleJson)

        assertThat(info).isNotNull()
        assertThat(info!!.versionTag).isEqualTo("v1.2.0")
        assertThat(info.releaseUrl).isEqualTo("https://github.com/ysompo/EnglishWords/releases/tag/v1.2.0")
        assertThat(info.apkDownloadUrl).isEqualTo("https://github.com/ysompo/EnglishWords/releases/download/v1.2.0/app-debug.apk")
    }

    @Test
    fun `returns null apk url when no apk asset is attached`() {
        val json = """
            {
              "tag_name": "v1.2.0",
              "html_url": "https://example.com",
              "assets": [
                { "name": "notes.txt", "browser_download_url": "https://example.com/notes.txt" }
              ]
            }
        """.trimIndent()

        val info = GitHubReleaseParser.parse(json)

        assertThat(info).isNotNull()
        assertThat(info!!.apkDownloadUrl).isNull()
    }

    @Test
    fun `returns null on malformed json instead of throwing`() {
        assertThat(GitHubReleaseParser.parse("not json at all")).isNull()
    }

    @Test
    fun `returns null when tag_name is missing`() {
        assertThat(GitHubReleaseParser.parse("""{"assets": []}""")).isNull()
    }
}
