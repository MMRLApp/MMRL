package com.dergoogler.mmrl.model.online

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Changelog(
    val versionName: String,
    val versionCode: Int,
    val changes: String,
    val preRelease: Boolean = false,
)
