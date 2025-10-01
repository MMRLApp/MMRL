package com.dergoogler.mmrl.model.json

import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.network.NetworkUtils
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter

@JsonClass(generateAdapter = true)
data class UpdateJson(
    val version: String,
    val versionCode: Int,
    val zipUrl: String,
    val size: Int = 0,
    val changelog: String = ""
) {
    fun toItemOrNull(timestamp: Float): VersionItem? {
        if (!NetworkUtils.isUrl(zipUrl)) return null

        val changelog = when {
            !NetworkUtils.isUrl(changelog) -> ""
            NetworkUtils.isBlobUrl(changelog) -> ""
            else -> changelog
        }

        return VersionItem(
            timestamp = timestamp,
            version = version,
            versionCode = versionCode,
            zipUrl = zipUrl,
            size = size,
            changelog = changelog
        )
    }

    companion object {
        suspend fun loadToVersionItem(url: String): VersionItem? {
            if (!NetworkUtils.isUrl(url)) return null

            val result = NetworkUtils.request(url) { body, headers ->
                val adapter = Moshi.Builder()
                    .build()
                    .adapter<UpdateJson>()

                adapter.fromJson(body.string()) to headers
            }

            if (result.isSuccess) {
                val (json, headers) = result.getOrThrow()
                if (json != null) {
                    val t = headers.getInstant("Last-Modified")?.toEpochMilli()
                    val timestamp = (t ?: 0L) / 1000f

                    return json.toItemOrNull(timestamp)
                }
            }

            return null
        }
    }
}