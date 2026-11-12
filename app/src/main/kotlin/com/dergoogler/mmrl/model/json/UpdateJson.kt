package com.dergoogler.mmrl.model.json

import android.util.Log
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
    val changelog: String = "",
) {
    fun toItemOrNull(timestamp: Float): VersionItem? {
        if (!NetworkUtils.isUrl(zipUrl)) return null

        val changelog =
            when {
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
            changelog = changelog,
        )
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun loadToVersionItem(url: String): VersionItem? {
            if (!NetworkUtils.isUrl(url)) return null

            val result = NetworkUtils.request(url) { body, headers ->
                val adapter = Moshi.Builder().build().adapter<UpdateJson>()
                adapter.fromJson(body.string()) to headers
            }

            if (!result.isSuccess) {
                Log.d("UpdateJson", "request failed: ${result.exceptionOrNull()}")
                return null
            }

            val (json, _) = result.getOrThrow()
            if (json == null) return null

            val zipResult = NetworkUtils.requestHead(json.zipUrl)

            if (!zipResult.isSuccess) {
                Log.d("UpdateJson", "zip head request failed: ${zipResult.exceptionOrNull()}")
                return null
            }

            val headers = zipResult.getOrThrow()

            val instant = headers.getInstant("Last-Modified") ?: headers.getInstant("Date")
            val t = instant?.toEpochMilli()
            val timestamp = (t ?: 0L) / 1000f

            return json.toItemOrNull(timestamp)
        }

    }
}
