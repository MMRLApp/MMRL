package com.dergoogler.mmrl.model.online

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class TrackJson(
    @Json(name = "type") val typeName: String,
    val added: Float? = 0f,
    val source: String = "",
    val antifeatures: List<String>? = null,
) {
    val type = TrackType.valueOf(typeName)
    val hasAntifeatures = antifeatures.orEmpty().isNotEmpty()
}

enum class TrackType {
    UNKNOWN,
    ONLINE_JSON,
    ONLINE_ZIP,
    GIT,
    LOCAL_JSON,
    LOCAL_ZIP,
}