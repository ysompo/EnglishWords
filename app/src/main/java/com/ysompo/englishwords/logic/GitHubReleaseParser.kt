package com.ysompo.englishwords.logic

import org.json.JSONException
import org.json.JSONObject

object GitHubReleaseParser {

    fun parse(json: String): LatestReleaseInfo? {
        return try {
            val obj = JSONObject(json)
            val tag = obj.getString("tag_name")
            val releaseUrl = obj.optString("html_url", "")
            val assets = obj.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", "").ifEmpty { null }
                        break
                    }
                }
            }
            LatestReleaseInfo(versionTag = tag, apkDownloadUrl = apkUrl, releaseUrl = releaseUrl)
        } catch (e: JSONException) {
            null
        }
    }
}
